package br.dev.allan.controlefinanceiro

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.dev.allan.controlefinanceiro.data.local.AppDatabase
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionDao
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transactionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertTransaction_andReadItBack() = runBlocking {
        val expense = TransactionEntity(title = "Teste", amount = 100.0, date = System.currentTimeMillis())
        dao.insertTransaction(expense)

        val allExpenses = dao.getAllTransactions().first()
        assertTrue(allExpenses.isNotEmpty())
        assertEquals("Teste", allExpenses[0].title)
    }

    @Test
    fun updateTransaction_changesPersisted() = runBlocking {
        val expense = TransactionEntity(title = "Original", amount = 50.0, date = System.currentTimeMillis())
        dao.insertTransaction(expense)

        val inserted = dao.getAllTransactions().first().first()
        val updated = inserted.copy(title = "Atualizado")
        dao.updateTransaction(updated)

        val allExpenses = dao.getAllTransactions().first()
        assertEquals("Atualizado", allExpenses[0].title)
    }

    @Test
    fun deleteTransaction_removesFromDatabase() = runBlocking {
        val expense = TransactionEntity(title = "Apagar", amount = 10.0, date = System.currentTimeMillis())
        dao.insertTransaction(expense)

        val inserted = dao.getAllTransactions().first().first()
        dao.deleteTransaction(inserted)

        val allExpenses = dao.getAllTransactions().first()
        assertTrue(allExpenses.isEmpty())
    }
}
