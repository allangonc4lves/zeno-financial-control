package br.dev.allan.controlefinanceiro.data.remote.model

data class CreditCardDto(
    val id: String = "",
    val bankName: String = "",
    val brand: String = "",
    val lastDigits: Int = 0,
    val invoiceClosing: Int = 1,
    val dueDate: Int = 10,
    val backgroundColor: Long = 0xFF000000,
    @field:JvmField
    val activated: Boolean = true,
    val userId: String = ""
)
