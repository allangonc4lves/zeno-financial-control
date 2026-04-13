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

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    fun getRecentTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE (date BETWEEN :start AND :end) 
        OR (isFixed = 1 AND date <= :end)
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>>

    @Query("""
    SELECT SUM(
        CASE 
            WHEN isInstallment = 1 AND installmentCount > 0 
            THEN amount / installmentCount 
            ELSE amount 
        END
    ) 
    FROM transactions 
    WHERE type = 'EXPENSE' 
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
""")
    fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?>

    @Query("""
    SELECT SUM(
        CASE 
            WHEN isInstallment = 1 AND installmentCount > 0 
            THEN amount / installmentCount 
            ELSE amount 
        END
    ) 
    FROM transactions 
    WHERE type = 'INCOME' 
    AND ((date BETWEEN :start AND :end) OR (isFixed = 1 AND date <= :end))
""")
    fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?>

    @Query("""
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
    WHERE type = 'EXPENSE' 
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
""")
    fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}