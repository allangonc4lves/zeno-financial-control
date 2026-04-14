package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val repository: CreditCardRepository
) : ViewModel() {

    val cards: StateFlow<List<CreditCard>> =
        repository.getCards().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addCard(bankName: String, brand: String, backgroundColor: Long) {
        val card = CreditCard(bankName = bankName, brand = brand, backgroundColor = backgroundColor)
        viewModelScope.launch { repository.addCard(card) }
    }

    fun updateCard(card: CreditCard) {
        viewModelScope.launch { repository.updateCard(card) }
    }

    fun removeCard(id: String) {
        viewModelScope.launch { repository.removeCard(id) }
    }
}