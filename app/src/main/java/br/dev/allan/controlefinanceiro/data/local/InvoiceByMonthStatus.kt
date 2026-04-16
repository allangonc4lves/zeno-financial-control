package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Entity

@Entity(
    tableName = "invoices_payment_status",
    primaryKeys = ["transactionId", "monthYear"]
)
data class PaymentStatusEntity(
    val transactionId: String,
    val monthYear: String
)
