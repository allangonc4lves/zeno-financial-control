package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.ui.graphics.Color

data class AddCreditCardUiState (
    val bankName: String = "",
    val brand: String = "",
    val lastDigits: String = "",
    val backgroundColor: Long = 0xFF1E88E5,

    val isLoading: Boolean = false,
    val bankNameError: String? = null,
    val brandError: String? = null,
    val lastDigitsError: String? = null
)