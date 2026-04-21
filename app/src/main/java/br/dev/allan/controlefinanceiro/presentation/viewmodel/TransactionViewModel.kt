package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.usecase.SaveTransactionUseCase
import br.dev.allan.controlefinanceiro.utils.ValidateAmount
import br.dev.allan.controlefinanceiro.utils.ValidateCategory
import br.dev.allan.controlefinanceiro.utils.ValidateText
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.TransactionAction
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.formatAmountForUi
import br.dev.allan.controlefinanceiro.utils.formatAsCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.text.replace

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val saveUseCase: SaveTransactionUseCase
) : ViewModel() {

    private var currentId: Int? = null
    private val _uiState = MutableStateFlow(TransactionUIModel())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<SaveTransactionUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            cardRepository.getCards().collect { cards ->
                _uiState.update { it.copy(cards = cards) }
            }
        }
    }

    fun onAction(action: TransactionAction) {
        when (action) {
            is TransactionAction.TitleChanged ->
                updateState { it.copy(title = action.value, titleError = null) }

            is TransactionAction.AmountChanged ->
                updateState { it.copy(amountInput = action.value.formatAsCurrency(), amountError = null) }

            is TransactionAction.CategoryChanged ->
                updateState { it.copy(category = action.value, categoryError = null) }

            is TransactionAction.DateChanged -> {
                updateState { it.copy(
                    dateDisplay = action.millis,
                    dateMillis = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(action.millis)?.time ?: it.dateMillis
                    } catch(e: Exception) { it.dateMillis }
                )}
            }

            is TransactionAction.TypeChanged ->
                updateState { it.copy(type = action.type) }

            is TransactionAction.DivideValueToggle ->
                updateState { it.copy(isDivideValue = action.divide) }

            is TransactionAction.InstallmentCountChanged ->
                updateState { it.copy(installmentCount = action.count.coerceIn(1, 360)) }

            is TransactionAction.CardSelected ->
                updateState { it.copy(creditCardId = action.cardId) }

            is TransactionAction.DirectionChanged ->
                updateState { it.copy(direction = action.dir, category = null) }

            is TransactionAction.PaidChanged ->
                updateState { it.copy(isPaid = action.paid) }

            is TransactionAction.CreditCardToggle ->
                updateState { it.copy(isCreditCard = action.isCreditCard, creditCardId = if (!action.isCreditCard) null else it.creditCardId) }

            is TransactionAction.Save -> save()

            is TransactionAction.Delete -> delete()
        }
    }

   fun save() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val result = saveUseCase.execute(_uiState.value, currentId)

            if (result.isSuccess) {
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
            } else {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    fun delete() {
        currentId?.let { id ->
            viewModelScope.launch {
                repository.deleteTransaction(id)
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
            }
        }
    }

    fun updateState(transform: (TransactionUIModel) -> TransactionUIModel) {
        _uiState.update(transform)
    }

    fun loadToEdit(id: Int) {
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { tx ->
                currentId = tx.id
                updateState { it.copy(
                    title = tx.title,
                    amountInput = formatAmountForUi(tx.amount),
                    dateDisplay = DateHelper.fromDbToUi(tx.date),
                    dateMillis = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)?.time ?: System.currentTimeMillis(),
                    category = tx.category,
                    direction = tx.direction,
                    isPaid = tx.isPaid,
                    creditCardId = tx.creditCardId,
                    type = if (tx.isInstallment || tx.type == TransactionType.REPEAT) tx.type else TransactionType.DEFAULT,
                    installmentCount = if (tx.isInstallment || tx.type == TransactionType.REPEAT) tx.installmentCount else 1,
                    isCreditCard = tx.creditCardId != null
                )}
            }
        }
    }

    fun resetState() {
        val currentCards = _uiState.value.cards
        currentId = null
        _uiState.value = TransactionUIModel(cards = currentCards)
    }

}
