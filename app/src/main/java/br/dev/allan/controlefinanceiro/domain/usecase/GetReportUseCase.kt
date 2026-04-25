package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportFilterState
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionStatusFilter
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionTypeFilter
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ReportTransactionOccurrence(
    val transaction: Transaction,
    val occurrenceDate: Long,
    val currentParcel: Int,
    val isPaidInMonth: Boolean,
    val amount: Double
)

data class ReportData(
    val occurrences: List<ReportTransactionOccurrence>,
    val totalIncome: Double,
    val totalExpense: Double
)

class GetReportUseCase @Inject constructor() {

    operator fun invoke(
        allTransactions: List<Transaction>,
        filters: ReportFilterState,
        payments: List<PaymentStatusEntity>
    ): ReportData {
        val occurrences = mutableListOf<ReportTransactionOccurrence>()

        allTransactions.forEach { tx ->
            if (filters.categoryFilter != null && tx.category.name != filters.categoryFilter) return@forEach

            val dates = getOccurrencesInRange(tx, filters.startDate, filters.endDate)

            dates.forEach { occurrenceDate ->
                val currentMonthYear = formatMillisToMonthYear(occurrenceDate)
                val isPaidInMonth = if (tx.creditCardId != null) {
                    payments.any { it.transactionId == tx.id.toString() && it.monthYear == currentMonthYear }
                } else {
                    tx.isPaid
                }

                if (matchesFilters(tx, isPaidInMonth, filters)) {
                    val actualAmount = getAmountForOccurrence(tx)
                    val actualParcel = if (tx.currentInstallment > 0) tx.currentInstallment else tx.getCurrentParcelIndex(occurrenceDate)
                    occurrences.add(
                        ReportTransactionOccurrence(
                            transaction = tx,
                            occurrenceDate = occurrenceDate,
                            currentParcel = actualParcel,
                            isPaidInMonth = isPaidInMonth,
                            amount = actualAmount
                        )
                    )
                }
            }
        }

        val totalIncome = occurrences
            .filter { it.transaction.direction == TransactionDirection.INCOME }
            .sumOf { it.amount }

        // Note: For expenses, we calculate the sum of all occurrences found. 
        // In the ViewModel, these will be grouped into Invoices if they belong to a credit card.
        val totalExpense = occurrences
            .filter { it.transaction.direction == TransactionDirection.EXPENSE }
            .sumOf { it.amount }

        return ReportData(occurrences, totalIncome, totalExpense)
    }

    private fun getAmountForOccurrence(tx: Transaction): Double {
        return if (tx.currentInstallment == 0 && tx.isInstallment && tx.installmentCount > 1 && tx.type != TransactionType.REPEAT) {
            (tx.amount / tx.installmentCount).round2()
        } else {
            tx.amount
        }
    }

    private fun Double.round2() = Math.round(this * 100.0) / 100.0

    private fun getOccurrencesInRange(tx: Transaction, start: Long, end: Long): List<Long> {
        val dates = mutableListOf<Long>()
        val calTx = Calendar.getInstance().apply {
            val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)
            time = dateObj ?: Date()
        }

        when {
            tx.currentInstallment > 0 -> {
                if (calTx.timeInMillis in start..end) {
                    dates.add(calTx.timeInMillis)
                }
            }
            tx.isInstallment && tx.installmentCount > 1 -> {
                for (i in 0 until tx.installmentCount) {
                    val calParcel = (calTx.clone() as Calendar).apply {
                        add(Calendar.MONTH, i)
                    }
                    if (calParcel.timeInMillis in start..end) {
                        dates.add(calParcel.timeInMillis)
                    }
                }
            }
            else -> {
                if (calTx.timeInMillis in start..end) {
                    dates.add(calTx.timeInMillis)
                }
            }
        }
        return dates
    }

    private fun matchesFilters(tx: Transaction, isPaid: Boolean, filters: ReportFilterState): Boolean {
        val matchesType = when (filters.typeFilter) {
            TransactionTypeFilter.ALL -> true
            TransactionTypeFilter.INCOME -> tx.direction == TransactionDirection.INCOME
            TransactionTypeFilter.EXPENSE -> tx.direction == TransactionDirection.EXPENSE
            TransactionTypeFilter.INVOICES_ONLY -> tx.creditCardId != null
            TransactionTypeFilter.WALLET_ONLY -> tx.creditCardId == null
        }

        val matchesStatus = when (filters.statusFilter) {
            TransactionStatusFilter.ALL -> true
            TransactionStatusFilter.PAID -> isPaid
            TransactionStatusFilter.UNPAID -> !isPaid
        }
        return matchesType && matchesStatus
    }
}
