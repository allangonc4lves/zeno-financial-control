package br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card

data class AddCreditCardUiState (
    val bankName: String = "",
    val brand: String = "",
    val lastDigits: String = "",
    val invoiceClosing: String = "",
    val dueDate: String = "",
    val backgroundColor: Long = 0xFF1E88E5,
    val bankNameError: String? = null,
    val brandError: String? = null,
    val lastDigitsError: String? = null,
    val isLoading: Boolean = false,
    val activated: Boolean = true,
    val palette: List<Long> = listOf(
        0xFF1E88E5L, // azul
        0xFF43A047L, // verde
        0xFFF4511EL, // laranja avermelhado
        0xFF6A1B9AL, // roxo
        0xFF00897BL, // teal
        0xFFCC092FL, // vermelho
        0xFF000000L, // preto
        0xFFFFD700L, // dourado
        0xFFC0C0C0L, // prata
        0xFF757575L, // cinza
        0xFFFF9800L  // laranja
    )
)