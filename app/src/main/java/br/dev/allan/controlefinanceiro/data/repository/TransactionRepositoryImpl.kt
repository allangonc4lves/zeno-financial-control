package br.dev.allan.controlefinanceiro.data.repository

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.data.local.mapper.toEntity
import br.dev.allan.controlefinanceiro.data.remote.TransactionRemoteDataSource
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val remoteDataSource: TransactionRemoteDataSource
): TransactionRepository {

    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
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
            transactions.map { it.toDomain(payments, monthYear) }
        }
    }

    override fun getTotalUnpaidForCard(cardId: String, invoiceClosingDay: Int): Flow<Double> {
        return combine(
            transactionDao.getByCard(cardId),
            transactionDao.getAllPaymentStatuses()
        ) { transactions, payments ->
            transactions.sumOf { entity ->
                val calendar = Calendar.getInstance().apply {
                    val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(entity.date)
                    time = dateObj ?: Date()
                }

                val transactionDay = calendar.get(Calendar.DAY_OF_MONTH)
                val transactionMonth = calendar.get(Calendar.MONTH)
                val transactionYear = calendar.get(Calendar.YEAR)

                val invoiceCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, transactionYear)
                    set(Calendar.MONTH, transactionMonth)
                    set(Calendar.DAY_OF_MONTH, invoiceClosingDay)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (transactionDay >= invoiceClosingDay) {
                    invoiceCal.add(Calendar.MONTH, 1)
                }

                val monthYear = formatMillisToMonthYear(invoiceCal.timeInMillis)

                val isPaidInMonth = payments.any {
                    it.transactionId == entity.id && it.monthYear == monthYear
                }

                if (!isPaidInMonth) entity.amount else 0.0
            }
        }
    }

    override fun getRecentTransactions(): Flow<List<Transaction>> {
        val tenDaysInMs = 10 * 24 * 60 * 60 * 1000L
        val dateCutoffMillis = System.currentTimeMillis() - tenDaysInMs

        val dateCutoffStr = DateHelper.fromMillisToDb(dateCutoffMillis)

        return transactionDao.getRecentTransactions(dateCutoffStr).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>> {

        val startDateStr = DateHelper.fromMillisToDb(start)
        val endDateStr = DateHelper.fromMillisToDb(end)

        return combine(
            transactionDao.getTransactionsByMonth(startDateStr, endDateStr),
            transactionDao.getAllPaymentStatuses()
        ) { entities, payments ->
            val monthYear = formatMillisToMonthYear(start)
            entities.map { it.toDomain(payments, monthYear) }
        }
    }

    override fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?> {
        val startDateStr = DateHelper.fromMillisToDb(start)
        val endDateStr = DateHelper.fromMillisToDb(end)
        return transactionDao.getTotalExpensesByMonth(startDateStr, endDateStr)
    }

    override fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?> {
        val startDateStr = DateHelper.fromMillisToDb(start)
        val endDateStr = DateHelper.fromMillisToDb(end)
        return transactionDao.getTotalIncomesByMonth(startDateStr, endDateStr)
    }

    override suspend fun updatePaymentStatus(id: String, paid: Boolean) {
        transactionDao.updatePaymentStatus(id, paid)
    }

    override suspend fun deleteTransactionGroup(groupId: String) {
        val transactionsToDelete = transactionDao.getTransactionsByGroupId(groupId)
        transactionDao.deleteTransactionGroup(groupId)
        remoteDataSource.deleteTransactions(transactionsToDelete.map { it.id })
    }

    override fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>> {
        return transactionDao.getExpensesByCategory(start, end)
    }

    override fun getTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }

    override fun getAllPaymentStatuses(): Flow<List<PaymentStatusEntity>> {
        return transactionDao.getAllPaymentStatuses()
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransactionById(id)
        remoteDataSource.deleteTransaction(id)
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
        remoteDataSource.saveTransaction(transaction)
    }

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        val entities = transactions.map { it.toEntity() }
        transactionDao.insertTransactions(entities)
        remoteDataSource.syncTransactions(transactions)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
        remoteDataSource.saveTransaction(transaction)
    }

    override suspend fun updateTransactionGroup(
        groupId: String,
        title: String,
        amount: Double,
        category: br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory,
        creditCardId: String?
    ) {
        transactionDao.updateTransactionGroup(groupId, title, amount, category.name, creditCardId)
        val updatedTransactions = transactionDao.getTransactionsByGroupId(groupId).map { it.toDomain() }
        remoteDataSource.syncTransactions(updatedTransactions)
    }

    override suspend fun updateCardIdByGroupId(groupId: String, cardId: String?) {
        transactionDao.updateCardIdByGroupId(groupId, cardId)
        val updatedTransactions = transactionDao.getTransactionsByGroupId(groupId).map { it.toDomain() }
        remoteDataSource.syncTransactions(updatedTransactions)
    }

    override suspend fun getTransactionsByGroupId(groupId: String): List<Transaction> {
        return transactionDao.getTransactionsByGroupId(groupId).map { it.toDomain() }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
        remoteDataSource.deleteTransaction(transaction.id)
    }
}

