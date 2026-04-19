package br.dev.allan.controlefinanceiro.data.repository

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.data.local.mapper.toEntity
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
): TransactionRepository {

    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    override fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>> {
        return combine(
            transactionDao.getTransactionsByMonth(start, end),
            transactionDao.getAllPaymentStatuses()
        ) { entities, payments ->
            val monthYear = formatMillisToMonthYear(start)

            entities.map { it.toDomain(payments, monthYear, viewedMonthMillis = start) }
        }
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
            transactionDao.getAllPaymentStatuses()
        ) { transactions, payments ->
            transactions.map { it.toDomain(payments, monthYear, viewedMonthMillis = it.date) }
        }
    }

    override suspend fun updatePaymentStatus(id: Int, paid: Boolean) {
        transactionDao.updatePaymentStatus(id, paid)
    }

    override fun getTotalUnpaidForCard(cardId: String): Flow<Double> {
        return combine(
            transactionDao.getByCard(cardId),
            transactionDao.getAllPaymentStatuses()
        ) { transactions, payments ->
            transactions.filter { entity ->
                val isPaid = payments.any { it.transactionId == entity.id.toString() }
                !isPaid
            }.sumOf { it.amount }
        }
    }

    override fun getTotalExpensesByMonth(start: Long, end: Long) = transactionDao.getTotalExpensesByMonth(start, end)

    override fun getTotalIncomesByMonth(start: Long, end: Long) = transactionDao.getTotalIncomesByMonth(start, end)

    override suspend fun deleteTransactionGroup(groupId: String) {
        transactionDao.deleteTransactionGroup(groupId)
    }

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

        return transactionDao.getRecentTransactions(dateCutoff).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllPaymentStatuses(): Flow<List<PaymentStatusEntity>> {
        return transactionDao.getAllPaymentStatuses()
    }

    override suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun deleteTransaction(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    override suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        val entities = transactions.map { it.toEntity() }
        transactionDao.insertTransactions(entities)
    }

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction.toEntity())
}
