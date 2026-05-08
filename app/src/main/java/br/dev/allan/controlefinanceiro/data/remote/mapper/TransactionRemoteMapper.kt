package br.dev.allan.controlefinanceiro.data.remote.mapper

import android.util.Log
import br.dev.allan.controlefinanceiro.data.remote.model.TransactionDto
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.constants.TransactionType

fun Transaction.toDto(userId: String): TransactionDto {
    return TransactionDto(
        id = this.id,
        groupId = this.groupId,
        title = this.title,
        amount = this.amount,
        date = this.date,
        category = this.category.name,
        type = this.type.name,
        isInstallment = this.isInstallment,
        installmentCount = this.installmentCount,
        currentInstallment = this.currentInstallment,
        isPaid = this.isPaid,
        direction = this.direction.name,
        creditCardId = this.creditCardId,
        userId = userId
    )
}

fun TransactionDto.toDomain(): Transaction {
    val categoryEnum = try {
        TransactionCategory.valueOf(this.category)
    } catch (e: Exception) {
        Log.e("SyncDebug", "Erro ao mapear categoria: '${this.category}' na transação '${this.title}'. Usando OTHERS_EXPENSE.")
        TransactionCategory.OTHERS_EXPENSE
    }

    val typeEnum = try {
        TransactionType.valueOf(this.type)
    } catch (e: Exception) {
        Log.e("SyncDebug", "Erro ao mapear tipo: '${this.type}'. Usando DEFAULT.")
        TransactionType.DEFAULT
    }

    val directionEnum = try {
        TransactionDirection.valueOf(this.direction)
    } catch (e: Exception) {
        Log.e("SyncDebug", "Erro ao mapear direção: '${this.direction}'. Usando EXPENSE.")
        TransactionDirection.EXPENSE
    }

    return Transaction(
        id = this.id,
        groupId = this.groupId,
        title = this.title,
        amount = this.amount,
        date = this.date,
        category = categoryEnum,
        type = typeEnum,
        isInstallment = this.isInstallment,
        installmentCount = this.installmentCount,
        currentInstallment = this.currentInstallment,
        isPaid = this.isPaid,
        direction = directionEnum,
        creditCardId = this.creditCardId
    )
}
