package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // --- QUERIES DE BUSCA (READ) ---

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :dateCutoff ORDER BY date DESC")
    fun getRecentTransactions(dateCutoff: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE (date BETWEEN :start AND :end) 
        OR (isFixed = 1 AND date <= :end)
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE direction = 'EXPENSE' 
        AND (
            (date BETWEEN :start AND :end) 
            OR (isFixed = 1 AND date <= :end)
        )
    """)
    fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?>

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE direction = 'INCOME' 
        AND (
            (date BETWEEN :start AND :end) 
            OR (isFixed = 1 AND date <= :end)
        )
    """)
    fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE direction = 'EXPENSE' 
        AND (
            (date BETWEEN :start AND :end) 
            OR (isFixed = 1 AND date <= :end)
        )
        GROUP BY category
    """)
    fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    @Query("SELECT * FROM transactions WHERE creditCardId = :cardId ORDER BY date DESC")
    fun getByCard(cardId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE direction = 'EXPENSE' AND category = 'CREDIT_CARD_PAYMENT' AND creditCardId = :cardId
        AND (
            (date BETWEEN :start AND :end)
            OR (isFixed = 1 AND date <= :end)
        )
    """)
    fun getTotalCardExpensesByMonth(cardId: String, start: Long, end: Long): Flow<Double?>

    // --- OPERAÇÕES DE ESCRITA (WRITE) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("DELETE FROM transactions WHERE groupId = :groupId")
    suspend fun deleteTransactionGroup(groupId: String)

    // --- STATUS DE PAGAMENTO ---

    @Query("UPDATE transactions SET isPaid = :paid WHERE id = :id")
    suspend fun updatePaymentStatus(id: Int, paid: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsPaid(payment: PaymentStatusEntity)

    @Query("DELETE FROM payment_status WHERE transactionId = :tId AND monthYear = :mY")
    suspend fun markAsUnpaid(tId: String, mY: String)

    @Query("SELECT * FROM payment_status")
    fun getAllPaymentStatuses(): Flow<List<PaymentStatusEntity>>

    // --- AUXILIARES ---

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
}