package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.data.local.mapper.toUi
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.CategoryAppearance
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import br.dev.allan.controlefinanceiro.domain.usecase.GetMonthlyTransactionsUseCase
import br.dev.allan.controlefinanceiro.utils.HomeUiState
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
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
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val currencyManager: CurrencyManager,
    private val settingsManager: SettingsManager,
    private val getMonthlyTransactionsUseCase: GetMonthlyTransactionsUseCase
) : ViewModel() {

    var selectedMonth by mutableStateOf(YearMonth.now())
        private set

    val uiState: StateFlow<HomeUiState> = combine(
        settingsManager.isBalanceVisible,
        settingsManager.currencyCode,
        repository.getTransactions(),
        repository.getAllPaymentStatuses(),
        snapshotFlow { selectedMonth }
    ) { isVisible, code, allTransactions, payments, month ->

        val monthYearStr = month.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"))
        val monthYearDbStr = month.format(java.time.format.DateTimeFormatter.ofPattern("MM-yyyy"))

        val monthlyTransactions = getMonthlyTransactionsUseCase(allTransactions, month).map { tx ->
            val isPaidInMonth = payments.any { 
                it.transactionId == tx.id.toString() && (it.monthYear == monthYearStr || it.monthYear == monthYearDbStr)
            }
            tx.copy(isPaid = tx.isPaid || isPaidInMonth)
        }

        // 2. Calcular valores numéricos (Double)
        val incomeVal = monthlyTransactions
            .filter { it.direction == TransactionDirection.INCOME }
            .sumOf { getMonthlyTransactionsUseCase.getAmountForMonth(it) }

        val expenseVal = monthlyTransactions
            .filter { it.direction == TransactionDirection.EXPENSE }
            .sumOf { getMonthlyTransactionsUseCase.getAmountForMonth(it) }

        val paidVal = monthlyTransactions
            .filter { it.isPaid && it.direction == TransactionDirection.EXPENSE }
            .sumOf { getMonthlyTransactionsUseCase.getAmountForMonth(it) }

        val pendingVal = monthlyTransactions
            .filter { !it.isPaid && it.direction == TransactionDirection.EXPENSE }
            .sumOf { getMonthlyTransactionsUseCase.getAmountForMonth(it) }

        val totalBalanceVal = incomeVal - expenseVal

        // 3. Preparar dados do gráfico
        val expensesByCategory = monthlyTransactions
            .filter { it.direction == TransactionDirection.EXPENSE }
            .groupBy { it.category.getAppearance() }
            .mapValues { entry ->
                entry.value.sumOf { getMonthlyTransactionsUseCase.getAmountForMonth(it) }
            }

        val chartLabels = expensesByCategory.mapValues { entry ->
            currencyManager.formatByCurrencyCode(entry.value, code)
        }

        // 4. Retornar o Estado Único
        HomeUiState(
            isBalanceVisible = isVisible,
            rawBalance = totalBalanceVal,
            balance = currencyManager.formatByCurrencyCode(totalBalanceVal, code),
            incomes = currencyManager.formatByCurrencyCode(incomeVal, code),
            expenses = currencyManager.formatByCurrencyCode(expenseVal, code),
            paidValue = currencyManager.formatByCurrencyCode(kotlin.math.abs(paidVal), code),
            pendingValue = currencyManager.formatByCurrencyCode(kotlin.math.abs(pendingVal), code),
            transactions = monthlyTransactions.take(10).map { it.toUi(currencyManager, code) },
            chartDataValues = expensesByCategory,
            chartDataLabels = chartLabels
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun updateMonth(newMonth: YearMonth) { selectedMonth = newMonth }

    fun toggleBalanceVisibility(isVisible: Boolean) {
        viewModelScope.launch { settingsManager.setBalanceVisible(isVisible) }
    }
}