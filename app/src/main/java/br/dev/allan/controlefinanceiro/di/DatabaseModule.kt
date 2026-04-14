package br.dev.allan.controlefinanceiro.di

import android.content.Context
import androidx.room.Room
import br.dev.allan.controlefinanceiro.data.local.AppDatabase
import br.dev.allan.controlefinanceiro.data.local.CreditCardDao
import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context, AppDatabase::class.java, "finance_db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCreditCardDao(db: AppDatabase): CreditCardDao = db.creditCardDao()

}