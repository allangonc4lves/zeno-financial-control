package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import br.dev.allan.controlefinanceiro.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: TransactionCategory,
    val isFixed: Boolean = false,
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val type: TransactionINorEX,
)
