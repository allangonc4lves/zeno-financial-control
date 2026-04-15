package br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card

sealed class SaveCreditCardUiEvent {
    object SaveSuccess : SaveCreditCardUiEvent()
}