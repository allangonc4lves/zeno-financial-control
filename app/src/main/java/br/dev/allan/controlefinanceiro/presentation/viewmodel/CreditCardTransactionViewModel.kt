package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardAmountByYear
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
        currencyCode
    ) { cardId, monthMillis, code ->
        val monthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(monthMillis))
        Triple(cardId, monthYear, code)
    }.flatMapLatest { (cardId, monthYear, code) ->
        if (cardId == null) return@flatMapLatest flowOf(emptyList<TransactionUIModel>())

        transactionRepository.getCreditCardTransactions(monthYear).map { allTransactions ->
            val calendar = Calendar.getInstance().apply { timeInMillis = _currentMonth.value }
            val startStr = DateHelper.fromMillisToDb(calendar.timeInMillis)

            calendar.add(Calendar.MONTH, 1)
            val endStr = DateHelper.fromMillisToDb(calendar.timeInMillis)

            allTransactions
                .filter { it.creditCardId == cardId }
                .filter { transaction ->
                    transaction.date >= startStr && transaction.date < endStr
                }
                .map { transaction ->
                    val roundedParcel = transaction.amount

                    val dateForUi = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(transaction.date) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    TransactionUIModel(
                        id = transaction.id,
                        title = transaction.title,
                        formattedParcelInfo = if (transaction.isInstallment) {
                            "${transaction.currentInstallment} / ${transaction.installmentCount}"
                        } else null,
                        formattedAmount = currencyManager.formatByCurrencyCode(roundedParcel, code),
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(transaction.amount, code),
                        formattedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(dateForUi),
                        color = if (transaction.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        isPaid = transaction.isPaid,
                        isInstallment = transaction.isInstallment,
                        currentInstallment = transaction.currentInstallment,
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
        currencyCode
    ) { cardId, monthMillis, code ->
        val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(monthMillis))
        Triple(cardId, monthMillis, monthYear)
    }.flatMapLatest { (cardId, monthMillis, monthYear) ->
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

                val monthStartStr = DateHelper.fromMillisToDb(monthCal.timeInMillis)
                val monthStartMillis = monthCal.timeInMillis

                monthCal.add(Calendar.MONTH, 1)
                val monthEndStr = DateHelper.fromMillisToDb(monthCal.timeInMillis)

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
        currencyCode
    ) { cardId, code ->
        cardId to code
    }.flatMapLatest { (cardId, code) ->
        if (cardId == null) {
            flowOf(currencyManager.formatByCurrencyCode(0.0, code))
        } else {
            transactionRepository.getTotalUnpaidForCard(cardId).map { total ->
                currencyManager.formatByCurrencyCode(total, code)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryChartData = combine(
        _selectedCardId,
        _currentMonth
    ) { cardId, monthMillis ->
        cardId to monthMillis
    }.flatMapLatest { (cardId, monthMillis) ->
        if (cardId == null) return@flatMapLatest flowOf(emptyMap<CategoryAppearance, Double>())

        val monthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(monthMillis))
        transactionRepository.getCreditCardTransactions(monthYear).map { allTransactions ->
            val calendar = Calendar.getInstance().apply { timeInMillis = monthMillis }
            val startStr = DateHelper.fromMillisToDb(calendar.timeInMillis)
            calendar.add(Calendar.MONTH, 1)
            val endStr = DateHelper.fromMillisToDb(calendar.timeInMillis)

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

    private val _uiEvent = kotlinx.coroutines.channels.Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun markMonthAsPaid(isPaid: Boolean) {
        viewModelScope.launch {
            val cardId = _selectedCardId.value ?: return@launch
            val monthMillis = _currentMonth.value

            val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(monthMillis))

            val calendar = Calendar.getInstance().apply { timeInMillis = monthMillis }
            val startStr = DateHelper.fromMillisToDb(calendar.timeInMillis)

            calendar.add(Calendar.MONTH, 1)
            val endStr = DateHelper.fromMillisToDb(calendar.timeInMillis)

            transactionRepository.getCreditCardTransactions(monthYear).first().let { allTransactions ->

                val transactionsInMonth = allTransactions
                    .filter { it.creditCardId == cardId }
                    .filter { it.date >= startStr && it.date < endStr }

                if (isPaid) {
                    val cal = Calendar.getInstance().apply { timeInMillis = monthMillis }
                    val start = cal.apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    val end = cal.timeInMillis

                    val allTransactionsInMonth = transactionRepository.getTransactionsByMonth(start, end).first()

                    val totalIncome = allTransactionsInMonth
                        .filter { it.direction == TransactionDirection.INCOME }
                        .sumOf { it.amount }

                    val totalPaidExpenses = allTransactionsInMonth
                        .filter { it.direction == TransactionDirection.EXPENSE && it.isPaid }
                        .sumOf { it.amount }

                    val invoiceTotal = transactionsInMonth.sumOf { it.amount }

                    if (totalPaidExpenses + invoiceTotal > totalIncome) {
                        _uiEvent.send("Saldo insuficiente para pagar esta fatura!")
                        return@launch
                    }
                }

                transactionsInMonth.forEach { transaction ->
                    if (isPaid) {
                        transactionRepository.markAsPaid(transaction.id.toString(), monthYear)
                    } else {
                        transactionRepository.markAsUnpaid(transaction.id.toString(), monthYear)
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