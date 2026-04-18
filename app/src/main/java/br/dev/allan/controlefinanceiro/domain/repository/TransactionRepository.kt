package br.dev.allan.controlefinanceiro.domain.repository

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>>
    fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?>
    fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?>
    fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>>
    fun getTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(): Flow<List<Transaction>>
    suspend fun updatePaymentStatus(id: Int, isPaid: Boolean)
    suspend fun incrementPaidInstallment(id: Int)
    suspend fun updateTransactionsPaymentStatus(ids: List<Int>, isPaid: Boolean)
    fun getCreditCardTransactions(monthYear: String? = null): Flow<List<Transaction>>
    fun getTotalUnpaidForCard(cardId: String): Flow<Double>
    suspend fun markAsPaid(transactionId: String, monthYear: String)
    suspend fun markAsUnpaid(transactionId: String, monthYear: String)
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    fun getAllPaymentStatuses(): Flow<List<PaymentStatusEntity>>
    suspend fun getTransactionById(id: Int): Transaction?
    suspend fun deleteTransaction(id: Int)
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}