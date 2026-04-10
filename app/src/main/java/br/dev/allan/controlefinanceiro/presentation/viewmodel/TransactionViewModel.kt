package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    var selectedMonth by mutableStateOf(YearMonth.now())
        private set

    private fun getMonthRange(month: YearMonth): Pair<Long, Long> {
        val start = month.atDay(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    // Flow para o Total de Despesas
    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpenses: StateFlow<Double> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getTotalExpensesByMonth(start, end)
        }
        .map { value: Double? -> value ?: 0.0 } // Especificamos o tipo (Double?) aqui
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalIncomes: StateFlow<Double> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getTotalIncomesByMonth(start, end)
        }
        .map { value: Double? -> value ?: 0.0 } // Especificamos o tipo (Double?) aqui
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalBalance: StateFlow<Double> = combine(totalIncomes, totalExpenses) { income, expense ->
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun updateMonth(newMonth: YearMonth) {
        selectedMonth = newMonth
    }
    val transactions = repository.getTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Se ficar sempre emptyList, o filtro nunca roda
        )
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.insertTransaction(transaction) }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.updateTransaction(transaction) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}