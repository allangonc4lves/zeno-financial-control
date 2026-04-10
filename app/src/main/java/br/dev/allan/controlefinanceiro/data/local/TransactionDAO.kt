package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Busca transações do mês OU fixas (desde que criadas antes ou no mês selecionado)
    @Query("""
        SELECT * FROM transactions 
        WHERE (date BETWEEN :start AND :end) 
        OR (isFixed = 1 AND date <= :end)
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>>

    // Soma Despesas: do mês OU fixas
    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'EXPENSE' 
        AND ((date BETWEEN :start AND :end) OR (isFixed = 1 AND date <= :end))
    """)
    fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?>

    // Soma Receitas: do mês OU fixas
    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'INCOME' 
        AND ((date BETWEEN :start AND :end) OR (isFixed = 1 AND date <= :end))
    """)
    fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}