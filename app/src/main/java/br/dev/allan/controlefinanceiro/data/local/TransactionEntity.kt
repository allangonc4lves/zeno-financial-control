package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: String? = null,
    val title: String,
    val amount: Double,
    val date: String,
    val category: TransactionCategory,
    val type: TransactionType,
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val currentInstallment: Int = 0,
    val isPaid: Boolean = false,
    val direction: TransactionDirection,
    val creditCardId: String? = null
)
