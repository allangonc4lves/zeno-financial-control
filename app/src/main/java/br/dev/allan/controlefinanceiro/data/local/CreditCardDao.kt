package br.dev.allan.controlefinanceiro.data.local

import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY bankName")
    fun observeAll(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<CreditCardEntity?>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCardById(id: String): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CreditCardEntity)

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteById(id: String)
}