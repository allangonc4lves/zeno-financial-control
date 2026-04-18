package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :dateCutoff ORDER BY date DESC")
    fun getRecentTransactions(dateCutoff: Long): Flow<List<Transaction>>

    @Query("""
    SELECT * FROM transactions 
    WHERE (date BETWEEN :start AND :end) 
    OR (isFixed = 1 AND date <= :end)
    OR (isInstallment = 1 AND date <= :end) -- Busca parcelas iniciadas antes do mês atual
    ORDER BY date DESC
""")
    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>>

    @Query(
        """
    SELECT SUM(
        CASE 
            WHEN isInstallment = 1 AND installmentCount > 0 
            THEN amount / installmentCount 
            ELSE amount 
        END
    ) 
    FROM transactions 
    WHERE direction = 'EXPENSE' 
    AND (
        (date BETWEEN :start AND :end) 

        OR (isFixed = 1 AND date <= :end)

        OR (
            isInstallment = 1 
            AND date <= :end 
            AND (
                ((:end - date) / 2629746000) < installmentCount
            )
        )
    )
"""
    )
    fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?>

    @Query(
        """
    SELECT SUM(
        CASE 
            WHEN isInstallment = 1 AND installmentCount > 0 
            THEN amount / installmentCount 
            ELSE amount 
        END
    ) 
    FROM transactions 
    WHERE direction = 'INCOME' 
    AND (
        (date BETWEEN :start AND :end) 

        OR (isFixed = 1 AND date <= :end)

        OR (
            isInstallment = 1 
            AND date <= :end 
            AND (
                ((:end - date) / 2629746000) < installmentCount
            )
        )
    )
    """
    )
    fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?>

    @Query(
        """
    SELECT 
        category, 
        SUM(
            CASE 
                WHEN isInstallment = 1 AND installmentCount > 0 
                THEN amount / installmentCount 
                ELSE amount 
            END
        ) as total 
    FROM transactions 
    WHERE direction = 'EXPENSE' 
    AND (
        
        (date BETWEEN :start AND :end) 

        OR (isFixed = 1 AND date <= :end)

        OR (
            isInstallment = 1 
            AND date <= :end 
            AND ((:end - date) / 2629746000) < installmentCount
        )
    )
    GROUP BY category
"""
    )
    fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    @Query("SELECT * FROM transactions WHERE creditCardId = :cardId ORDER BY date DESC")
    fun getByCard(cardId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(
            CASE WHEN isInstallment = 1 AND installmentCount > 0 THEN amount / installmentCount ELSE amount END
        ) FROM transactions
        WHERE direction = 'EXPENSE' AND category = 'CREDIT_CARD_PAYMENT' AND creditCardId = :cardId
        AND (
            (date BETWEEN :start AND :end)
            OR (isFixed = 1 AND date <= :end)
            OR (
                isInstallment = 1 AND date <= :end AND (((:end - date) / 2629746000) < installmentCount)
            )
        )
    """)
    fun getTotalCardExpensesByMonth(cardId: String, start: Long, end: Long): Flow<Double?>

    @Query("UPDATE transactions SET isPaid = :paid WHERE id = :id")
    suspend fun updatePaymentStatus(id: Int, paid: Boolean)

    @Query("UPDATE transactions SET paidInstallments = paidInstallments + 1 WHERE id = :id AND paidInstallments < installmentCount")
    suspend fun incrementPaidInstallment(id: Int)

    @Query("UPDATE transactions SET paidInstallments = paidInstallments - 1 WHERE id = :id AND paidInstallments > 0")
    suspend fun decrementPaidInstallment(id: Int)

    @Query("UPDATE transactions SET isPaid = :isPaid WHERE id IN (:ids)")
    suspend fun updateTransactionsPaymentStatus(ids: List<Int>, isPaid: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsPaid(payment: PaymentStatusEntity)

    @Query("DELETE FROM invoices_payment_status WHERE transactionId = :tId AND monthYear = :mY")
    suspend fun markAsUnpaid(tId: String, mY: String)

    @Query("SELECT * FROM invoices_payment_status")
    fun getAllPaymentStatus(): Flow<List<PaymentStatusEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("SELECT * FROM invoices_payment_status")
    fun getAllPaymentStatuses(): Flow<List<PaymentStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}