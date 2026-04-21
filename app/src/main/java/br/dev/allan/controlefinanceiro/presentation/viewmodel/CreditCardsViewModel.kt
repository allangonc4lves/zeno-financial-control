package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.utils.ValidateLastDigitsCreditCard
import br.dev.allan.controlefinanceiro.utils.ValidateText
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.AddCreditCardUiState
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.SaveCreditCardUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreditCardsViewModel @Inject constructor(
    private val validateText: ValidateText = ValidateText(),
    private val validateLastDigits: ValidateLastDigitsCreditCard = ValidateLastDigitsCreditCard(),
    private val repository: CreditCardRepository
) : ViewModel() {
    private var currentCardId: String? = null
    var uiState by mutableStateOf(AddCreditCardUiState())
        private set

    private val _uiEvent = Channel<SaveCreditCardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onBankNameChange(newBankName: String) {
        uiState = uiState.copy(bankName = newBankName, bankNameError = null)
    }

    fun onBrandChange(newBrand: String) {
        uiState = uiState.copy(brand = newBrand, brandError = null)
    }

    fun onLastDigitsChange(newLastDigits: String) {
            uiState = uiState.copy(lastDigits = newLastDigits, lastDigitsError = null)
            Log.i("teste", uiState.lastDigits)
    }

    fun onColorSelected(color: Long) {
        uiState = uiState.copy(backgroundColor = color)
    }

    val cards: StateFlow<List<CreditCard>> =
        repository.getCards().stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    fun loadCardToEdit(id: String) {
        viewModelScope.launch {
            repository.getCardById(id)?.let { card ->
                currentCardId = card.id
                uiState = uiState.copy(
                    bankName = card.bankName,
                    brand = card.brand,
                    lastDigits = card.lastDigits.toString(),
                    backgroundColor = card.backgroundColor
                )
            }
        }
    }

    fun saveCard() {
        uiState = uiState.copy(isLoading = true)

        val bankNameResult = validateText.execute(uiState.bankName)
        val brandResult = validateText.execute(uiState.brand)
        val lastDigitsResult = validateLastDigits.execute(uiState.lastDigits)

        if (listOf(bankNameResult, brandResult, lastDigitsResult).any { !it.successful }) {
            uiState = uiState.copy(
                isLoading = false,
                bankNameError = bankNameResult.errorMessage,
                brandError = brandResult.errorMessage,
                lastDigitsError = lastDigitsResult.errorMessage
            )
            return
        }

        val card = CreditCard(
            id = currentCardId ?: UUID.randomUUID().toString(),
            bankName = uiState.bankName,
            brand = uiState.brand,
            lastDigits = uiState.lastDigits.toIntOrNull() ?: 0,
            dueDate = uiState.dueDate,
            backgroundColor = uiState.backgroundColor,
            activated = uiState.activated
        )

        viewModelScope.launch {
            if (currentCardId == null) {
                repository.addCard(card)
            } else {
                repository.updateCard(card)
            }

            _uiEvent.send(SaveCreditCardUiEvent.SaveSuccess)
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun removeCard() {
        currentCardId?.let { id ->
            viewModelScope.launch {
                repository.removeCard(id)
                _uiEvent.send(SaveCreditCardUiEvent.SaveSuccess)
            }
        }
    }

    fun resetState() {
        currentCardId = null
        uiState = AddCreditCardUiState()
    }
}