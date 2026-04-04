package br.dev.allan.controlefinanceiro

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.dev.allan.controlefinanceiro.data.local.AppDatabase
import br.dev.allan.controlefinanceiro.data.local.Expense
import br.dev.allan.controlefinanceiro.data.local.ExpenseDao
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ExpenseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.expenseDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertExpense_andReadItBack() = runBlocking {
        val expense = Expense(title = "Teste", amount = 100.0, date = System.currentTimeMillis())
        dao.insertExpense(expense)

        val allExpenses = dao.getAllExpenses().first()
        assertTrue(allExpenses.isNotEmpty())
        assertEquals("Teste", allExpenses[0].title)
    }

    @Test
    fun updateExpense_changesPersisted() = runBlocking {
        val expense = Expense(title = "Original", amount = 50.0, date = System.currentTimeMillis())
        dao.insertExpense(expense)

        val inserted = dao.getAllExpenses().first().first()
        val updated = inserted.copy(title = "Atualizado")
        dao.updateExpense(updated)

        val allExpenses = dao.getAllExpenses().first()
        assertEquals("Atualizado", allExpenses[0].title)
    }

    @Test
    fun deleteExpense_removesFromDatabase() = runBlocking {
        val expense = Expense(title = "Apagar", amount = 10.0, date = System.currentTimeMillis())
        dao.insertExpense(expense)

        val inserted = dao.getAllExpenses().first().first()
        dao.deleteExpense(inserted)

        val allExpenses = dao.getAllExpenses().first()
        assertTrue(allExpenses.isEmpty())
    }
}
