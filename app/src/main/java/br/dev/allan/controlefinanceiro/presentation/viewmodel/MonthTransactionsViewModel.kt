package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.local.mapper.toUi
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import kotlinx.coroutines.flow.first
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MonthTransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val currencyManager: CurrencyManager,
    private val settingsManager: br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
) : ViewModel() {

    private val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)

    val currentMonth = _currentMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionsUiModel: StateFlow<List<TransactionUIState>> = kotlinx.coroutines.flow.combine(
        _currentMonth,
        currencyCode
    ) { monthMillis, code ->
        val (start, end) = getMonthRange(monthMillis)
        val startStr = DateHelper.fromMillisToDb(start)
        val endStr = DateHelper.fromMillisToDb(end)

        repository.getTransactionsByMonth(start, end).map { list ->
            list.filter { transaction ->
                if (transaction.creditCardId != null) return@filter false

                when {
                    transaction.date > endStr -> false
                    transaction.isInstallment -> !transaction.isExpired(monthMillis)
                    else -> true
                }
            }.map { it.toUi(currencyManager, code, monthMillis) }
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiEvent = kotlinx.coroutines.channels.Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val messageResId: Int, val formatArgs: List<String> = emptyList()) : UiEvent()
    }

    fun togglePayment(uiModel: TransactionUIState) {
        viewModelScope.launch {
            val id = uiModel.id

            // Bloqueio para compras no cartão
            if (uiModel.creditCardId != null && uiModel.direction == TransactionDirection.EXPENSE) {
                val card = cardRepository.getCardById(uiModel.creditCardId)
                val cardName = card?.bankName ?: "Card"
                _uiEvent.send(UiEvent.ShowSnackbar(br.dev.allan.controlefinanceiro.R.string.card_payment_error, listOf(cardName)))
                return@launch
            }

            // Bloqueio por saldo insuficiente para despesas diretas (Carteira)
            if (!uiModel.isPaid && uiModel.direction == TransactionDirection.EXPENSE) {
                val transactions = transactionsUiModel.value
                val totalIncome = transactions
                    .filter { it.direction == TransactionDirection.INCOME }
                    .sumOf { it.amount }
                
                val totalPaidExpenses = transactions
                    .filter { it.direction == TransactionDirection.EXPENSE && it.isPaid }
                    .sumOf { it.amount }

                if (totalPaidExpenses + uiModel.amount > totalIncome) {
                    _uiEvent.send(UiEvent.ShowSnackbar(br.dev.allan.controlefinanceiro.R.string.insufficient_balance_payment))
                    return@launch
                }
            }

            if (uiModel.type == TransactionType.DEFAULT) {
                val monthYear = formatMillisToMonthYear(currentMonth.value)

                if (uiModel.isPaid) {
                    repository.markAsUnpaid(id, monthYear)
                } else {
                    repository.markAsPaid(id, monthYear)
                }
            } else {
                repository.updatePaymentStatus(id, !uiModel.isPaid)
            }
        }
    }

    fun updateTransaction(updated: TransactionUIState, editAll: Boolean) {
        viewModelScope.launch {
            val original = repository.getTransactionById(updated.id) ?: return@launch
            
            if (editAll && (original.isInstallment || original.type == TransactionType.REPEAT)) {
                val groupId = original.groupId
                if (groupId != null) {
                    val allTransactions = repository.getTransactions().first()
                    val transactionsInGroup = allTransactions.filter { it.groupId == groupId }
                    transactionsInGroup.forEach { tx ->
                        repository.updateTransaction(
                            tx.copy(
                                title = updated.title,
                                amount = updated.amount,
                                isPaid = updated.isPaid
                            )
                        )
                    }
                }
            } else {
                repository.updateTransaction(
                    original.copy(
                        title = updated.title,
                        amount = updated.amount,
                        isPaid = updated.isPaid
                    )
                )
            }
        }
    }

    fun changeMonth(increment: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _currentMonth.value }
        cal.add(Calendar.MONTH, increment)
        _currentMonth.value = cal.timeInMillis
    }

    private fun getMonthRange(monthMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = monthMillis }
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}

