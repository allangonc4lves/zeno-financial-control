package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

data class CreditCardAmountByYear(
    val monthName: String,
    val totalValue: Double,
    val isSelected: Boolean,
    val isPaid: Boolean
)