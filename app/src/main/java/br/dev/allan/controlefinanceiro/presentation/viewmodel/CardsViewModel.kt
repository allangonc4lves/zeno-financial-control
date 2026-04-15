package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateLastDigitsCreditCard
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateText
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.AddCreditCardUiState
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.SaveCreditCardUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val validateText: ValidateText = ValidateText(),
    private val validateLastDigits: ValidateLastDigitsCreditCard = ValidateLastDigitsCreditCard(),
    private val repository: CreditCardRepository
) : ViewModel() {
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

    fun saveCard() {
        uiState = uiState.copy(isLoading = true)

        val bankNameResult = validateText.execute(uiState.bankName)
        val brandResult = validateText.execute(uiState.brand)
        val lastDigitsResult = validateLastDigits.execute(uiState.lastDigits)

        val hasError = listOf(bankNameResult, brandResult, lastDigitsResult).any { !it.successful }

        if (hasError) {
            uiState = uiState.copy(isLoading = false)
            uiState = uiState.copy(
                bankNameError = bankNameResult.errorMessage,
                brandError = brandResult.errorMessage,
            )
            return
        } else {
            val card = CreditCard(
                bankName = uiState.bankName,
                brand = uiState.brand,
                lastDigits = uiState.lastDigits.toIntOrNull() ?: 1234,
                backgroundColor = uiState.backgroundColor
            )

            viewModelScope.launch {
                repository.addCard(card)

                delay(2000L)
                _uiEvent.send(SaveCreditCardUiEvent.SaveSuccess)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun updateCard(card: CreditCard) {
        viewModelScope.launch { repository.updateCard(card) }
    }

    fun removeCard(id: String) {
        viewModelScope.launch { repository.removeCard(id) }
    }
}