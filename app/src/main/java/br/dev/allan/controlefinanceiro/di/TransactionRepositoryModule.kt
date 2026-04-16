package br.dev.allan.controlefinanceiro.di

import br.dev.allan.controlefinanceiro.data.repository.CreditCardRepositoryImpl
import br.dev.allan.controlefinanceiro.data.repository.TransactionRepositoryImpl
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCreditCardRepository(impl: CreditCardRepositoryImpl): CreditCardRepository

}
