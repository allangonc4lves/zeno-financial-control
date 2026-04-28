package br.dev.allan.controlefinanceiro.data.remote.model

data class CreditCardDto(
    val id: String = "",
    val bankName: String = "",
    val brand: String = "",
    val lastDigits: Int = 0,
    val invoiceClosing: String = "",
    val dueDate: String = "",
    val backgroundColor: Long = 0xFF000000,
    @field:JvmField
    val activated: Boolean = true,
    val userId: String = ""
)
