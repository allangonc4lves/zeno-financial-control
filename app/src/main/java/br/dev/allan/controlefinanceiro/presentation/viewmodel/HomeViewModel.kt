package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.settings.SettingsManager
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.presentation.ui.model.CategoryAppearance
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {
    val isBalanceVisible = settingsManager.isBalanceVisible
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleBalanceVisibility(isVisible: Boolean) {
        viewModelScope.launch {
            settingsManager.setBalanceVisible(isVisible)
        }
    }

    var selectedMonth by mutableStateOf(YearMonth.now())
        private set

    private fun getMonthRange(month: YearMonth): Pair<Long, Long> {
        val start = month.atDay(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpenses: StateFlow<Double> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getTotalExpensesByMonth(start, end)
        }
        .map { value: Double? -> value ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = 0.0
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalIncomes: StateFlow<Double> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getTotalIncomesByMonth(start, end)
        }
        .map { value: Double? -> value ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalBalance: StateFlow<Double> = combine(totalIncomes, totalExpenses) { income, expense ->
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun updateMonth(newMonth: YearMonth) {
        selectedMonth = newMonth
    }
    val transactions = repository.getTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<Transaction>> =
        repository.getRecentTransactions()
            .map { it }
            .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chartData: StateFlow<Map<CategoryAppearance, Double>> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getExpensesByCategory(start, end)
        }
        .map { list ->
            list.associate { item ->
                item.category.getAppearance() to item.total
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

}