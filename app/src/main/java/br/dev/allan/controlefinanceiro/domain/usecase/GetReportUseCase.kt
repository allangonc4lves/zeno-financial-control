package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
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
    val amount: Double,
    val invoiceMonthYear: String? = null
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
        payments: List<PaymentStatusEntity>,
        cards: List<CreditCard>
    ): ReportData {
        val occurrences = mutableListOf<ReportTransactionOccurrence>()

        allTransactions.forEach { tx ->
            if (filters.categoryFilter != null && tx.category.name != filters.categoryFilter) return@forEach

            val card = cards.find { it.id == tx.creditCardId }
            val closingDay = card?.invoiceClosing ?: 1

            val dates = getOccurrencesInRange(tx, filters.startDate, filters.endDate, closingDay)

            dates.forEach { occurrenceInfo ->
                val (occurrenceDate, invoiceMonthMillis) = occurrenceInfo
                val invoiceMonthYear = formatMillisToMonthYear(invoiceMonthMillis)
                
                val isPaidInMonth = if (tx.creditCardId != null) {
                    payments.any { it.transactionId == tx.id && it.monthYear == invoiceMonthYear }
                } else {
                    tx.isPaid
                }

                if (matchesFilters(tx, isPaidInMonth, filters)) {
                    val actualAmount = getAmountForOccurrence(tx)
                    
                    val actualParcel = if (tx.currentInstallment > 0) {
                        tx.currentInstallment
                    } else if (tx.creditCardId != null) {
                        tx.getParcelIndexForInvoiceMonth(invoiceMonthMillis, closingDay)
                    } else {
                        tx.getCurrentParcelIndex(occurrenceDate)
                    }
                    
                    if (tx.creditCardId == null || actualParcel in 1..tx.installmentCount || tx.type == TransactionType.REPEAT) {
                        occurrences.add(
                            ReportTransactionOccurrence(
                                transaction = tx,
                                occurrenceDate = occurrenceDate,
                                currentParcel = actualParcel,
                                isPaidInMonth = isPaidInMonth,
                                amount = actualAmount,
                                invoiceMonthYear = if (tx.creditCardId != null) invoiceMonthYear else null
                            )
                        )
                    }
                }
            }
        }

        val totalIncome = occurrences
            .filter { it.transaction.direction == TransactionDirection.INCOME }
            .sumOf { it.amount }

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

    private fun getOccurrencesInRange(tx: Transaction, start: Long, end: Long, closingDay: Int): List<Pair<Long, Long>> {
        val dates = mutableListOf<Pair<Long, Long>>()
        val calTx = Calendar.getInstance().apply {
            val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)
            time = dateObj ?: Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (tx.creditCardId != null) {
            val firstInvoiceMonthMillis = tx.getInvoiceMonthStart(closingDay)
            
            if (tx.currentInstallment > 0) {
                if (firstInvoiceMonthMillis in start..end) {
                    dates.add(calTx.timeInMillis to firstInvoiceMonthMillis)
                }
            } else if (tx.isInstallment && tx.installmentCount > 1) {
                for (i in 0 until tx.installmentCount) {
                    val invoiceMonthCal = Calendar.getInstance().apply {
                        timeInMillis = firstInvoiceMonthMillis
                        add(Calendar.MONTH, i)
                    }
                    if (invoiceMonthCal.timeInMillis in start..end) {
                        dates.add(calTx.timeInMillis to invoiceMonthCal.timeInMillis)
                    }
                }
            } else if (tx.type == TransactionType.REPEAT) {
                for (i in 0 until 24) {
                    val invoiceMonthCal = Calendar.getInstance().apply {
                        timeInMillis = firstInvoiceMonthMillis
                        add(Calendar.MONTH, i)
                    }
                    if (invoiceMonthCal.timeInMillis > end) break
                    if (invoiceMonthCal.timeInMillis in start..end) {
                        dates.add(calTx.timeInMillis to invoiceMonthCal.timeInMillis)
                    }
                }
            } else {
                if (firstInvoiceMonthMillis in start..end) {
                    dates.add(calTx.timeInMillis to firstInvoiceMonthMillis)
                }
            }
        } else {
            when {
                tx.currentInstallment > 0 -> {
                    if (calTx.timeInMillis in start..end) {
                        dates.add(calTx.timeInMillis to calTx.timeInMillis)
                    }
                }
                tx.isInstallment && tx.installmentCount > 1 -> {
                    for (i in 0 until tx.installmentCount) {
                        val calParcel = (calTx.clone() as Calendar).apply {
                            add(Calendar.MONTH, i)
                        }
                        if (calParcel.timeInMillis in start..end) {
                            dates.add(calParcel.timeInMillis to calParcel.timeInMillis)
                        }
                    }
                }
                else -> {
                    if (calTx.timeInMillis in start..end) {
                        dates.add(calTx.timeInMillis to calTx.timeInMillis)
                    }
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
