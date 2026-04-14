package br.dev.allan.controlefinanceiro.data.local

import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY bankName")
    fun observeAll(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<CreditCardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CreditCardEntity)

    @Update
    suspend fun update(entity: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteById(id: String)
}