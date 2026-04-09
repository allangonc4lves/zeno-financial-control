package br.dev.allan.controlefinanceiro.data.repository

import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.data.local.mapper.toEntity
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val dao: TransactionDao) {

    fun getTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    suspend fun insertTransaction(transaction: Transaction) =
        dao.insertTransaction(transaction.toEntity())

    suspend fun updateTransaction(transaction: Transaction) =
        dao.updateTransaction(transaction.toEntity())

    suspend fun deleteTransaction(transaction: Transaction) =
        dao.deleteTransaction(transaction.toEntity())
}