package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.usecase.SaveTransactionUseCase
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.TransactionAction
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.formatAmountForUi
import br.dev.allan.controlefinanceiro.utils.formatAsCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val saveUseCase: SaveTransactionUseCase
) : ViewModel() {

    private var currentId: String? = null
    private val _uiState = MutableStateFlow(TransactionUIState())
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

            is TransactionAction.Save -> save(action.editAll)

            is TransactionAction.Delete -> delete()
        }
    }

    fun save(editAll: Boolean = false) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val result = saveUseCase.execute(_uiState.value, currentId, editAll)

            if (result.isSuccess) {
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                updateState { state ->
                    val titleRes = saveUseCase.validateText.execute(state.title)
                    val amountRes = saveUseCase.validateAmount.execute(state.amountInput)
                    val catRes = saveUseCase.validateCategory.execute(state.category)

                    state.copy(
                        isLoading = false,
                        titleError = titleRes.errorMessageRes?.let { "error_res_$it" },
                        amountError = amountRes.errorMessageRes?.let { "error_res_$it" },
                        categoryError = catRes.errorMessageRes?.let { "error_res_$it" }
                    )
                }
            }
        }
    }

    fun delete() {
        currentId?.let { id ->
            viewModelScope.launch {
                val groupId = _uiState.value.groupId
                if (groupId != null) {
                    repository.deleteTransactionGroup(groupId)
                } else {
                    repository.deleteTransaction(id)
                }
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
            }
        }
    }

    fun updateState(transform: (TransactionUIState) -> TransactionUIState) {
        _uiState.update(transform)
    }

    fun loadToEdit(id: String) {
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { tx ->
                currentId = tx.id
                updateState { it.copy(
                    id = tx.id,
                    groupId = tx.groupId,
                    title = tx.title,
                    amountInput = formatAmountForUi(tx.amount),
                    dateDisplay = DateHelper.fromDbToUi(tx.date),
                    dateMillis = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.date)?.time ?: System.currentTimeMillis(),
                    category = tx.category,
                    direction = tx.direction,
                    isPaid = tx.isPaid,
                    creditCardId = tx.creditCardId,
                    type = if (tx.isInstallment || tx.type == TransactionType.REPEAT) tx.type else TransactionType.DEFAULT,
                    currentInstallment = tx.currentInstallment,
                    installmentCount = if (tx.isInstallment || tx.type == TransactionType.REPEAT) tx.installmentCount else 1,
                    isCreditCard = tx.creditCardId != null
                )}
            }
        }
    }

    fun resetState() {
        val currentCards = _uiState.value.cards
        currentId = null
        _uiState.value = TransactionUIState(cards = currentCards)
    }

}
