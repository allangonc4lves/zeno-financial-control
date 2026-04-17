package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardAmountByYear
import br.dev.allan.controlefinanceiro.util.CurrencyManager
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
    private val currencyManager: CurrencyManager // Agora está private com sucesso!
) : ViewModel() {

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
            val startOfMonth = calendar.timeInMillis
            val endOfMonth = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis

            allTransactions
                .filter { it.creditCardId == cardId }
                .filter { transaction ->
                    if (transaction.isInstallment) {
                        !transaction.isExpired(_currentMonth.value) && transaction.date < endOfMonth
                    } else {
                        transaction.date in startOfMonth until endOfMonth
                    }
                }
                .map { transaction ->
                    val currentParcel = transaction.getCurrentParcelIndex(_currentMonth.value)

                    val rawParcelValue = if (transaction.isInstallment && transaction.installmentCount > 0) {
                        transaction.amount / transaction.installmentCount
                    } else {
                        transaction.amount
                    }
                    val roundedParcel = Math.round(rawParcelValue * 100.0) / 100.0

                    TransactionUIModel(
                        id = transaction.id,
                        title = transaction.title,
                        formattedParcelInfo = if (transaction.isInstallment) "$currentParcel / ${transaction.installmentCount}" else null,
                        formattedAmount = currencyManager.formatByCurrencyCode(roundedParcel, code),
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(transaction.amount, code),
                        formattedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(transaction.date)),
                        color = if (transaction.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        isPaid = transaction.isPaid,
                        isInstallment = transaction.isInstallment,
                        creditCardId = transaction.creditCardId,
                        category = transaction.category,
                        direction = transaction.direction
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
        val monthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(monthMillis))
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
                }
                val monthStart = monthCal.timeInMillis
                val monthEnd = monthCal.apply { add(Calendar.MONTH, 1) }.timeInMillis

                val transactionsInMonth = allTransactions
                    .filter { it.creditCardId == cardId }
                    .filter { transaction ->
                        if (transaction.isInstallment) {
                            !transaction.isExpired(monthStart) && transaction.date < monthEnd
                        } else {
                            transaction.date in monthStart until monthEnd
                        }
                    }

                val totalForMonth = transactionsInMonth.sumOf {
                    it.amount / (if (it.isInstallment) it.installmentCount else 1)
                }

                CreditCardAmountByYear(
                    monthName = SimpleDateFormat("MMM", Locale("pt", "BR")).format(Date(monthStart)),
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

    fun markMonthAsPaid(isPaid: Boolean) {
        viewModelScope.launch {
            val cardId = _selectedCardId.value ?: return@launch
            val monthMillis = _currentMonth.value
            val monthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date(monthMillis))

            transactionRepository.getCreditCardTransactions(monthYear).first().let { allTransactions ->
                val calendar = Calendar.getInstance().apply { timeInMillis = monthMillis }
                val startOfMonth = calendar.timeInMillis
                val endOfMonth = calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis

                val transactionsInMonth = allTransactions
                    .filter { it.creditCardId == cardId }
                    .filter { transaction ->
                        if (transaction.isInstallment) {
                            !transaction.isExpired(monthMillis) && transaction.date < endOfMonth
                        } else {
                            transaction.date in startOfMonth until endOfMonth
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