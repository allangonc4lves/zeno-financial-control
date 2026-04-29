package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.CreditCardAmountByYear
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.domain.model.CategoryAppearance
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreditCardTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val settingsManager: SettingsManager,
    private val currencyManager: CurrencyManager
) : ViewModel() {
    private fun Double.round2() = Math.round(this * 100.0) / 100.0

    data class CreditCardParams(
        val cardId: String?,
        val monthYear: String,
        val currencyCode: String,
        val closingDay: Int,
        val monthMillis: Long
    )

    private fun getPeriodForMonth(monthMillis: Long, closingDay: Int): Pair<String, String> {
        val calendar = Calendar.getInstance().apply { timeInMillis = monthMillis }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, closingDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startCal = (endCal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }

        return DateHelper.fromMillisToDb(startCal.timeInMillis) to DateHelper.fromMillisToDb(endCal.timeInMillis)
    }

    private val _selectedCardId = MutableStateFlow<String?>(null)
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis)

    val currentMonth = _currentMonth.asStateFlow()

    val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    val cards = cardRepository.getCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionsUiState = combine(
        _selectedCardId,
        _currentMonth,
        currencyCode,
        cards
    ) { cardId, monthMillis, code, allCards ->
        val card = allCards.find { it.id == cardId }
        val closingDay = card?.invoiceClosing ?: 1

        val period = getPeriodForMonth(monthMillis, closingDay)
        val monthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(monthMillis))
        val params = CreditCardParams(cardId, monthYear, code, closingDay, monthMillis)
        Triple(params, period.first, period.second)
    }.flatMapLatest { (params, startStr, endStr) ->
        val (cardId, monthYear, code, closingDay, monthMillis) = params
        if (cardId == null) return@flatMapLatest flowOf(emptyList<TransactionUIState>())

        transactionRepository.getCreditCardTransactions(monthYear).map { allTransactions ->
            allTransactions
                .filter { it.creditCardId == cardId }
                .filter { transaction ->
                    transaction.date >= startStr && transaction.date < endStr
                }
                .map { transaction ->
                    val roundedParcel = transaction.amount
                    val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val dateForUi = try {
                        dbFormat.parse(transaction.date) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    val actualParcel = if (transaction.currentInstallment > 0) {
                        transaction.currentInstallment
                    } else {
                        transaction.getParcelIndexForInvoiceMonth(monthMillis, closingDay)
                    }

                    TransactionUIState(
                        id = transaction.id,
                        title = transaction.title,
                        formattedParcelInfo = if (transaction.isInstallment || transaction.installmentCount > 1) {
                            "$actualParcel/${transaction.installmentCount}"
                        } else null,
                        formattedAmount = currencyManager.formatByCurrencyCode(roundedParcel, code),
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(transaction.amount, code),
                        formattedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(dateForUi),
                        dateMillis = dateForUi.time,
                        color = if (transaction.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        isPaid = transaction.isPaid,
                        isInstallment = transaction.isInstallment,
                        currentInstallment = actualParcel,
                        installmentCount = transaction.installmentCount,
                        creditCardId = transaction.creditCardId,
                        category = transaction.category,
                        direction = transaction.direction,
                        amount = roundedParcel,
                        type = transaction.type,
                    )
                }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chartDataState = combine(
        _selectedCardId,
        _currentMonth,
        currencyCode,
        cards
    ) { cardId, monthMillis, code, allCards ->
        val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(monthMillis))
        val card = allCards.find { it.id == cardId }
        Triple(Triple(cardId, monthMillis, monthYear), card?.invoiceClosing ?: 1, code)
    }.flatMapLatest { (triple, closingDay, _) ->
        val (cardId, monthMillis, monthYear) = triple
        if (cardId == null) return@flatMapLatest flowOf(emptyList<CreditCardAmountByYear>())

        transactionRepository.getCreditCardTransactions(monthYear).map { allTransactions ->
            val cal = Calendar.getInstance().apply { timeInMillis = monthMillis }
            val selectedYear = cal.get(Calendar.YEAR)
            val selectedMonth = cal.get(Calendar.MONTH)

            (0..11).map { monthIndex ->
                val monthCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, monthIndex)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }

                val monthStartMillis = monthCal.timeInMillis
                val period = getPeriodForMonth(monthStartMillis, closingDay)
                val monthStartStr = period.first
                val monthEndStr = period.second

                val transactionsInMonth = allTransactions
                    .filter { it.creditCardId == cardId }
                    .filter { it.date >= monthStartStr && it.date < monthEndStr }

                val totalForMonth = transactionsInMonth.sumOf { it.amount }

                CreditCardAmountByYear(
                    monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(Date(monthStartMillis))
                        .replaceFirstChar { it.uppercase() },
                    totalValue = totalForMonth,
                    isSelected = monthIndex == selectedMonth,
                    isPaid = transactionsInMonth.isNotEmpty() && transactionsInMonth.all { it.isPaid }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val formattedSelectedMonthTotal = combine(
        chartDataState,
        currencyCode
    ) { data, code ->
        val total = data.find { it.isSelected }?.totalValue ?: 0.0
        currencyManager.formatByCurrencyCode(total, code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalOpenInvoicesState = combine(
        _selectedCardId,
        currencyCode,
        cards
    ) { cardId, code, allCards ->
        Triple(cardId, code, allCards)
    }.flatMapLatest { (cardId, code, allCards) ->
        if (cardId == null) {
            flowOf(currencyManager.formatByCurrencyCode(0.0, code))
        } else {
            val card = allCards.find { it.id == cardId }
            val closingDay = card?.invoiceClosing ?: 1

            transactionRepository.getTotalUnpaidForCard(cardId, closingDay).map { total ->
                currencyManager.formatByCurrencyCode(total, code)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryChartData = combine(
        _selectedCardId,
        _currentMonth,
        cards
    ) { cardId, monthMillis, allCards ->
        val card = allCards.find { it.id == cardId }
        val period = getPeriodForMonth(monthMillis, card?.invoiceClosing ?: 1)
        Triple(cardId, monthMillis, period)
    }.flatMapLatest { (cardId, monthMillis, period) ->
        if (cardId == null) return@flatMapLatest flowOf(emptyMap<CategoryAppearance, Double>())

        val monthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(monthMillis))
        transactionRepository.getCreditCardTransactions(monthYear).map { allTransactions ->
            val (startStr, endStr) = period

            allTransactions
                .filter { it.creditCardId == cardId && it.date >= startStr && it.date < endStr }
                .groupBy { it.category }
                .mapKeys { it.key.getAppearance() }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryChartLabels = combine(
        categoryChartData,
        currencyCode
    ) { map, code ->
        map.mapValues { currencyManager.formatByCurrencyCode(it.value, code) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _uiEvent = kotlinx.coroutines.channels.Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val messageResId: Int) : UiEvent()
    }

    fun markMonthAsPaid(isPaid: Boolean) {
        viewModelScope.launch {
            val cardId = _selectedCardId.value ?: return@launch
            val monthMillis = _currentMonth.value
            
            val card = cards.value.find { it.id == cardId }
            val period = getPeriodForMonth(monthMillis, card?.invoiceClosing ?: 1)
            val (startStr, endStr) = period

            val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(monthMillis))

            transactionRepository.getCreditCardTransactions(monthYear).first().let { allTransactions ->

                val transactionsInMonth = allTransactions
                    .filter { it.creditCardId == cardId }
                    .filter { it.date >= startStr && it.date < endStr }

                if (isPaid) {
                    val cal = Calendar.getInstance().apply { timeInMillis = monthMillis }
                    val start = cal.apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    val end = cal.timeInMillis

                    val allTransactionsInMonth = transactionRepository.getTransactionsByMonth(start, end).first()

                    val totalIncome = allTransactionsInMonth
                        .filter { it.direction == TransactionDirection.INCOME }
                        .sumOf { it.amount }

                    val totalPaidExpenses = allTransactionsInMonth
                        .filter { it.direction == TransactionDirection.EXPENSE && it.isPaid && it.creditCardId == null }
                        .sumOf { it.amount }

                    val otherPaidInvoices = allTransactionsInMonth
                        .filter { it.direction == TransactionDirection.EXPENSE && it.isPaid && it.creditCardId != null }
                        .sumOf { it.amount }

                    val invoiceTotal = transactionsInMonth.sumOf { it.amount }

                    if (totalPaidExpenses + otherPaidInvoices + invoiceTotal > totalIncome) {
                        _uiEvent.send(UiEvent.ShowSnackbar(br.dev.allan.controlefinanceiro.R.string.insufficient_balance_card))
                        return@launch
                    }
                }

                transactionsInMonth.forEach { transaction ->
                    if (isPaid) {
                        transactionRepository.markAsPaid(transaction.id, monthYear)
                    } else {
                        transactionRepository.markAsUnpaid(transaction.id, monthYear)
                    }
                }
            }
        }
    }

    fun setSelectedCard(id: String) { _selectedCardId.value = id }

    fun changeMonth(increment: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _currentMonth.value }
        cal.add(Calendar.MONTH, increment)
        _currentMonth.value = cal.timeInMillis
    }
}