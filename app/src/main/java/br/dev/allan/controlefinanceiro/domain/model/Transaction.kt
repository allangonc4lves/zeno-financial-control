package br.dev.allan.controlefinanceiro.domain.model

data class Transaction(
    val id: String = "",
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long
)

enum class TransactionType {
    INCOME, EXPENSE
}
