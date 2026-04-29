package br.dev.allan.controlefinanceiro.domain.model

import java.util.UUID

data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val bankName: String,
    val brand: String,
    val lastDigits: Int,
    val invoiceClosing: Int,
    val dueDate: Int,
    val backgroundColor: Long,
    val activated: Boolean
)