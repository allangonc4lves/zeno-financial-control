package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey val id: String,
    val bankName: String,
    val brand: String,
    val lastDigits: Int,
    val invoiceClosing: String,
    val dueDate: String,
    val backgroundColor: Long,
    val activated: Boolean
)