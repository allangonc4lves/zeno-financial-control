package br.dev.allan.controlefinanceiro.data.repository

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.data.local.mapper.toEntity
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
): TransactionRepository {

    override fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByMonth(start, end)
    }

    override suspend fun updateTransactionsPaymentStatus(ids: List<Int>, isPaid: Boolean) {
        transactionDao.updateTransactionsPaymentStatus(ids, isPaid)
    }

    override suspend fun markAsPaid(transactionId: String, monthYear: String) {
        val paymentStatus = PaymentStatusEntity(
            transactionId = transactionId,
            monthYear = monthYear
        )
        transactionDao.markAsPaid(paymentStatus)
    }

    override suspend fun markAsUnpaid(transactionId: String, monthYear: String) {
        transactionDao.markAsUnpaid(transactionId, monthYear)
    }

    override fun getCreditCardTransactions(monthYear: String?): Flow<List<Transaction>> {
        return combine(
            transactionDao.getAllTransactions(),
            transactionDao.getAllPaymentStatus()
        ) { transactions, payments ->

            transactions.map { entity ->
                entity.toDomain(
                    payments = payments,
                    monthYear = monthYear
                )
            }
        }
    }

    override fun getTotalExpensesByMonth(start: Long, end: Long) = transactionDao.getTotalExpensesByMonth(start, end)

    override fun getTotalIncomesByMonth(start: Long, end: Long) = transactionDao.getTotalIncomesByMonth(start, end)

    override fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>> {
        return transactionDao.getExpensesByCategory(start, end)
    }

    override fun getTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }

    override fun getRecentTransactions(): Flow<List<Transaction>> {
        val tenDaysInMs = 10 * 24 * 60 * 60 * 1000L
        val dateCutoff = System.currentTimeMillis() - tenDaysInMs

        return transactionDao.getRecentTransactions(dateCutoff)
    }

    override suspend fun updatePaymentStatus(id: Int, isPaid: Boolean) {
        transactionDao.updatePaymentStatus(id, isPaid)
    }

    override suspend fun incrementPaidInstallment(id: Int) {
        transactionDao.incrementPaidInstallment(id)
    }

    override suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction.toEntity())
}

