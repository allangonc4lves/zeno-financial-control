package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.presentation.ui.model.CategoryAppearance
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance
import br.dev.allan.controlefinanceiro.util.CurrencyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val currencyManager: CurrencyManager,
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
    val totalIncomes: StateFlow<Double> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getTotalIncomesByMonth(start, end)
        }
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpenses: StateFlow<Double> = snapshotFlow { selectedMonth }
        .flatMapLatest { month ->
            val (start, end) = getMonthRange(month)
            repository.getTotalExpensesByMonth(start, end)
        }
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    val totalBalance: StateFlow<Double> = combine(totalIncomes, totalExpenses) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val formattedIncomes: StateFlow<String> = combine(
        totalIncomes,
        settingsManager.currencyCode
    ) { value, code ->
        currencyManager.formatByCurrencyCode(value, code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    val formattedExpenses: StateFlow<String> = combine(
        totalExpenses,
        settingsManager.currencyCode
    ) { value, code ->
        currencyManager.formatByCurrencyCode(value, code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    val formattedBalance: StateFlow<String> = combine(
        totalBalance,
        settingsManager.currencyCode
    ) { value, code ->
        currencyManager.formatByCurrencyCode(value, code)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    val formattedCategoryExpenses: StateFlow<Map<CategoryAppearance, String>> = combine(
        chartData,
        settingsManager.currencyCode
    ) { map, code ->
        map.mapValues { currencyManager.formatByCurrencyCode(it.value, code) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun updateMonth(newMonth: YearMonth) {
        selectedMonth = newMonth
    }
    val transactions = repository.getTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentTransactionsUI: StateFlow<List<TransactionUIModel>> = combine(
        repository.getRecentTransactions(),
        settingsManager.currencyCode
    ) { transactions, code ->
        transactions.map { item ->
            val prefix = if (item.direction == TransactionDirection.EXPENSE) "- " else "+ "

            val dynamicTitle = item.getDisplayTitle(System.currentTimeMillis())

            TransactionUIModel(
                id = item.id,
                title = dynamicTitle,
                formattedAmount = prefix + currencyManager.formatByCurrencyCode(item.amount, code),
                formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.date)),
                color = if (item.direction == TransactionDirection.EXPENSE) Color(0xFFAB1A1A) else Color(0xFF1B5E20),
                category = item.category,
                direction = item.direction,
                isPaid = if (item.isInstallment) {
                    val currentParcel = item.getCurrentParcelIndex(System.currentTimeMillis())
                    currentParcel <= item.paidInstallments
                } else {
                    item.isPaid
                },
                isInstallment = item.isInstallment,
                creditCardId = item.creditCardId,
                formattedParcelInfo = null,
                formattedTotalAmount = currencyManager.formatByCurrencyCode(item.amount, code),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

}