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
    suspend fun deleteTransactionGroup(groupId: String)
    suspend fun updatePaymentStatus(id: String, paid: Boolean)
    fun getCreditCardTransactions(monthYear: String? = null): Flow<List<Transaction>>
    fun getTotalUnpaidForCard(cardId: String, invoiceClosingDay: Int): Flow<Double>
    suspend fun markAsPaid(transactionId: String, monthYear: String)
    suspend fun markAsUnpaid(transactionId: String, monthYear: String)
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    fun getAllPaymentStatuses(): Flow<List<PaymentStatusEntity>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun deleteTransaction(id: String)
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun insertTransactions(transactions: List<Transaction>)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun updateTransactionGroup(
        groupId: String,
        title: String,
        amount: Double,
        category: br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory,
        creditCardId: String?
    )
    suspend fun updateCardIdByGroupId(groupId: String, cardId: String?)
    suspend fun getTransactionsByGroupId(groupId: String): List<Transaction>
    suspend fun deleteTransaction(transaction: Transaction)
}