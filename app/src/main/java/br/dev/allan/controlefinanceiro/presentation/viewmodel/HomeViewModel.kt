package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.data.local.mapper.toUi
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import br.dev.allan.controlefinanceiro.domain.usecase.GetMonthlyTransactionsUseCase
import br.dev.allan.controlefinanceiro.presentation.ui.state.HomeUiState
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.abs

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

        val referenceDate = month.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val monthlyTransactions = getMonthlyTransactionsUseCase(allTransactions, month).map { tx ->
            val calendar = java.util.Calendar.getInstance().apply {
                val dateObj = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(tx.date)
                time = dateObj ?: java.util.Date()
            }
            val currentMonthYear = br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear(calendar.timeInMillis)

            val isPaidInMonth = if (tx.creditCardId != null) {
                payments.any { it.transactionId == tx.id.toString() && it.monthYear == currentMonthYear }
            } else {
                tx.isPaid
            }
            tx.copy(isPaid = isPaidInMonth)
        }

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
        val availableBalanceVal = incomeVal - paidVal

        val expensesByCategory = monthlyTransactions
            .filter { it.direction == TransactionDirection.EXPENSE }
            .groupBy { it.category.getAppearance() }
            .mapValues { entry ->
                entry.value.sumOf { getMonthlyTransactionsUseCase.getAmountForMonth(it) }
            }

        val chartLabels = expensesByCategory.mapValues { entry ->
            currencyManager.formatByCurrencyCode(entry.value, code)
        }

        HomeUiState(
            isBalanceVisible = isVisible,
            rawBalance = totalBalanceVal,
            balance = currencyManager.formatByCurrencyCode(totalBalanceVal, code),
            rawAvailableBalance = availableBalanceVal,
            availableBalance = currencyManager.formatByCurrencyCode(availableBalanceVal, code),
            incomes = currencyManager.formatByCurrencyCode(incomeVal, code),
            expenses = currencyManager.formatByCurrencyCode(expenseVal, code),
            paidValue = currencyManager.formatByCurrencyCode(abs(paidVal), code),
            pendingValue = currencyManager.formatByCurrencyCode(abs(pendingVal), code),
            transactions = monthlyTransactions.take(10).map { it.toUi(currencyManager, code, referenceDate) },
            chartDataValues = expensesByCategory,
            chartDataLabels = chartLabels
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun updateMonth(newMonth: YearMonth) { selectedMonth = newMonth }

    fun toggleBalanceVisibility(isVisible: Boolean) {
        viewModelScope.launch { settingsManager.setBalanceVisible(isVisible) }
    }
}
