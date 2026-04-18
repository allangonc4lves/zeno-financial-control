package br.dev.allan.controlefinanceiro.data.repository

import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.data.local.CreditCardDao
import br.dev.allan.controlefinanceiro.data.local.CreditCardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val dao: CreditCardDao
) : CreditCardRepository {

    override fun getCards(): Flow<List<CreditCard>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getCardById(id: String): CreditCard? {
        return dao.getCardById(id)?.toDomain()
    }

    override suspend fun updateCard(card: CreditCard) {
        dao.updateCard(card.toEntity())
    }

    override suspend fun addCard(card: CreditCard) {
        dao.insert(card.toEntity())
    }

    override suspend fun removeCard(id: String) {
        dao.deleteById(id)
    }

    private fun CreditCardEntity.toDomain(): CreditCard =
        CreditCard(id = id, bankName = bankName, brand = brand, backgroundColor = backgroundColor, lastDigits = lastDigits)

    private fun CreditCard.toEntity(): CreditCardEntity =
        CreditCardEntity(id = id, bankName = bankName, brand = brand, backgroundColor = backgroundColor, lastDigits = lastDigits)
}