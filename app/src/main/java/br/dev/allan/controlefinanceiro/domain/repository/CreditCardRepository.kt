package br.dev.allan.controlefinanceiro.domain.repository

import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun getCards(): Flow<List<CreditCard>>
    suspend fun getCardById(id: String): CreditCard?
    suspend fun addCard(card: CreditCard)
    suspend fun updateCard(card: CreditCard)
    suspend fun removeCard(id: String)
}