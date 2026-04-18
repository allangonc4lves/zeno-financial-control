package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.data.local.CreditCardEntity
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.model.Transaction // IMPORTANTE: Seu model de domínio
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.util.CurrencyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TransactionTypeFilter { ALL, INCOME, EXPENSE }
enum class TransactionStatusFilter { ALL, PAID, UNPAID }

data class ReportFilterState(
    val startDate: Long,
    val endDate: Long,
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val statusFilter: TransactionStatusFilter = TransactionStatusFilter.ALL
)

data class ReportUIState(
    val items: List<ReportItem> = emptyList(),
    val formattedTotalIncome: String = "",
    val formattedTotalExpense: String = "",
    val formattedBalance: String = "",
    val isLoading: Boolean = false
)

sealed class ReportItem {
    abstract val dateForSorting: Long

    data class Transaction(
        val model: TransactionUIModel,
        override val dateForSorting: Long
    ) : ReportItem()

    data class Invoice(
        val cardId: String,
        val cardName: String,
        val monthYear: String,
        val totalAmount: Double,
        val formattedAmount: String,
        val isPaid: Boolean,
        val transactions: List<TransactionUIModel>,
        override val dateForSorting: Long
    ) : ReportItem()
}

fun Double.round2() = Math.round(this * 100.0) / 100.0

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val settingsManager: SettingsManager,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val _filterState = MutableStateFlow(getDefaultMonthRange())
    val filterState = _filterState.asStateFlow()

    private val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportUiState = combine(
        _filterState,
        currencyCode,
        transactionRepository.getAllPaymentStatuses(),
        cardRepository.getCards()
    ) { filters, code, payments, cards ->
        transactionRepository.getTransactions().map { allTransactions ->
            processReportData(allTransactions, filters, code, payments, cards)
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUIState(isLoading = true))
    private fun processReportData(
        allTransactions: List<Transaction>,
        filters: ReportFilterState,
        code: String,
        payments: List<PaymentStatusEntity>,
        cards: List<CreditCard>
    ): ReportUIState {
        val reportItems = mutableListOf<ReportItem>()
        val creditCardGroups = mutableMapOf<String, Pair<Long, MutableList<TransactionUIModel>>>()

        allTransactions.forEach { tx ->
            val occurrences = getOccurrencesInRange(tx, filters.startDate, filters.endDate)

            occurrences.forEach { occurrenceDate ->
                val currentMonthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(occurrenceDate))

                val isPaidInThisMonth = if (tx.creditCardId != null) {
                    payments.any { it.transactionId == tx.id.toString() && it.monthYear == currentMonthYear }
                } else {
                    tx.isPaid
                }

                val matchesType = when (filters.typeFilter) {
                    TransactionTypeFilter.ALL -> true
                    TransactionTypeFilter.INCOME -> tx.direction == TransactionDirection.INCOME
                    TransactionTypeFilter.EXPENSE -> tx.direction == TransactionDirection.EXPENSE
                }

                val matchesStatus = when (filters.statusFilter) {
                    TransactionStatusFilter.ALL -> true
                    TransactionStatusFilter.PAID -> isPaidInThisMonth
                    TransactionStatusFilter.UNPAID -> !isPaidInThisMonth
                }

                if (matchesType && matchesStatus) {
                    val currentParcel = tx.getCurrentParcelIndex(occurrenceDate)
                    val rawParcel = if (tx.isInstallment && tx.installmentCount > 0) tx.amount / tx.installmentCount else tx.amount
                    val roundedParcel = Math.round(rawParcel * 100.0) / 100.0

                    val uiModel = TransactionUIModel(
                        id = tx.id,
                        title = tx.title,
                        formattedAmount = currencyManager.formatByCurrencyCode(roundedParcel, code),
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(tx.amount, code),
                        formattedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(occurrenceDate)),
                        formattedParcelInfo = if (tx.isInstallment) "$currentParcel/${tx.installmentCount}" else null,
                        color = if (tx.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        isPaid = isPaidInThisMonth,
                        isInstallment = tx.isInstallment,
                        creditCardId = tx.creditCardId,
                        category = tx.category,
                        direction = tx.direction,
                        amount = roundedParcel
                    )

                    if (tx.creditCardId != null) {
                        val key = "${tx.creditCardId}_$currentMonthYear"
                        if (!creditCardGroups.containsKey(key)) creditCardGroups[key] = Pair(occurrenceDate, mutableListOf())
                        creditCardGroups[key]?.second?.add(uiModel)
                    } else {
                        reportItems.add(ReportItem.Transaction(uiModel, occurrenceDate))
                    }
                }
            }
        }

        creditCardGroups.forEach { (key, groupData) ->
            val dateSort = groupData.first
            val txs = groupData.second
            val total = txs.sumOf { it.amount }
            val cardId = txs.first().creditCardId ?: ""

            val cardInfo = cards.find { it.id == cardId }
            val displayName = if (cardInfo != null) {
                "${cardInfo.bankName} (${cardInfo.brand})" // Ex: Nubank (Mastercard)
            } else {
                "Cartão Removido"
            }

            reportItems.add(ReportItem.Invoice(
                cardId = cardId,
                cardName = displayName,
                monthYear = key.split("_")[1],
                totalAmount = total,
                formattedAmount = currencyManager.formatByCurrencyCode(total, code),
                isPaid = txs.all { it.isPaid },
                transactions = txs,
                dateForSorting = dateSort
            ))
        }

        val totalIncome = reportItems
            .filterIsInstance<ReportItem.Transaction>()
            .filter { it.model.direction == TransactionDirection.INCOME }
            .sumOf { it.model.amount }


        val totalExpense = reportItems.sumOf { item ->
            when (item) {
                is ReportItem.Transaction -> {
                    if (item.model.direction == TransactionDirection.EXPENSE) item.model.amount else 0.0
                }
                is ReportItem.Invoice -> item.totalAmount
            }
        }

        return ReportUIState(
            items = reportItems.sortedByDescending { it.dateForSorting },
            formattedTotalIncome = currencyManager.formatByCurrencyCode(totalIncome, code),
            formattedTotalExpense = currencyManager.formatByCurrencyCode(totalExpense, code),
            formattedBalance = currencyManager.formatByCurrencyCode(totalIncome - totalExpense, code),
            isLoading = false
        )
    }

    fun updateTypeFilter(type: TransactionTypeFilter) { _filterState.update { it.copy(typeFilter = type) } }
    fun updateStatusFilter(status: TransactionStatusFilter) { _filterState.update { it.copy(statusFilter = status) } }
    fun updateDateRange(start: Long, end: Long) { _filterState.update { it.copy(startDate = start, endDate = end) } }

    private fun getDefaultMonthRange(): ReportFilterState {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return ReportFilterState(startDate = start, endDate = cal.timeInMillis)
    }

    private fun getOccurrencesInRange(tx: Transaction, start: Long, end: Long): List<Long> {
        val dates = mutableListOf<Long>()

        val calStart = Calendar.getInstance().apply { timeInMillis = start }
        val calEnd = Calendar.getInstance().apply { timeInMillis = end }

        val calTx = Calendar.getInstance().apply { timeInMillis = tx.date }

        when {
            tx.isFixed -> {
                val tempCal = calStart.clone() as Calendar
                while (tempCal.before(calEnd) || isSameMonth(tempCal, calEnd)) {
                    if (!isBeforeMonth(tempCal, calTx)) {
                        dates.add(tempCal.timeInMillis)
                    }
                    tempCal.add(Calendar.MONTH, 1)
                }
            }

            tx.isInstallment -> {
                for (i in 0 until tx.installmentCount) {
                    val calParcel = (calTx.clone() as Calendar).apply { add(Calendar.MONTH, i) }
                    if (calParcel.timeInMillis in start..end || isSameMonth(calParcel, calStart) || isSameMonth(calParcel, calEnd)) {
                        dates.add(calParcel.timeInMillis)
                    }
                }
            }

            // CASO: COMUM
            else -> {
                if (tx.date in start..end) dates.add(tx.date)
            }
        }
        return dates
    }

    private fun isSameMonth(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
    }

    private fun isBeforeMonth(current: Calendar, reference: Calendar): Boolean {
        if (current.get(Calendar.YEAR) < reference.get(Calendar.YEAR)) return true
        if (current.get(Calendar.YEAR) > reference.get(Calendar.YEAR)) return false
        return current.get(Calendar.MONTH) < reference.get(Calendar.MONTH)
    }

    private fun getStartOfMonth(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun isSameMonth(d1: Long, d2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = d1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = d2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }
}