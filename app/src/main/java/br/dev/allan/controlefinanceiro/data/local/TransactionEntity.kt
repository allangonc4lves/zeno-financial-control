package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: String? = null,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: TransactionCategory,
    val type: TransactionType,
    val isFixed: Boolean = false,
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val currentInstallment: Int = 0,
    val isPaid: Boolean = false,
    val direction: TransactionDirection,
    val creditCardId: String? = null
)
