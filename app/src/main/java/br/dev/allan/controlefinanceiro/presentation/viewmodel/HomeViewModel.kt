package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import br.dev.allan.controlefinanceiro.domain.usecase.GetMonthlyTransactionsUseCase
import br.dev.allan.controlefinanceiro.presentation.ui.state.HomeUiState
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.data.local.mapper.toUi
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportItemUiModel
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
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
    private val cardRepository: br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository,
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
        cardRepository.getCards(),
        snapshotFlow { selectedMonth }
    ) { params: Array<Any?> ->
        val isVisible = params[0] as Boolean
        val code = params[1] as String
        val allTransactions = params[2] as List<Transaction>
        val payments = params[3] as List<PaymentStatusEntity>
        val allCards = params[4] as List<CreditCard>
        val month = params[5] as YearMonth

        val startOfMonth = month.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = month.atEndOfMonth().atTime(23, 59, 59, 999).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val reportData = br.dev.allan.controlefinanceiro.domain.usecase.GetReportUseCase().invoke(
            allTransactions = allTransactions,
            filters = br.dev.allan.controlefinanceiro.presentation.ui.state.ReportFilterState(
                startDate = startOfMonth,
                endDate = endOfMonth
            ),
            payments = payments,
            cards = allCards
        )

        val reportItems = mutableListOf<ReportItemUiModel>()
        val creditCardGroups = mutableMapOf<String, Pair<Long, MutableList<TransactionUIState>>>()
        val ccExpensesByCategory = mutableMapOf<br.dev.allan.controlefinanceiro.domain.model.CategoryAppearance, Double>()
        var totalCcInvoices = 0.0
        var paidCcInvoices = 0.0

        reportData.occurrences.forEach { occurrence ->
            val tx = occurrence.transaction
            val uiModel = tx.toUi(currencyManager, code).copy(
                amount = occurrence.amount,
                dateMillis = occurrence.occurrenceDate,
                formattedDate = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault()).format(java.util.Date(occurrence.occurrenceDate)),
                formattedParcelInfo = if (tx.isInstallment || tx.installmentCount > 1) "${occurrence.currentParcel}/${tx.installmentCount}" else null,
                isPaid = occurrence.isPaidInMonth,
                formattedAmount = (if (tx.direction == TransactionDirection.EXPENSE) "- " else "+ ") + currencyManager.formatByCurrencyCode(occurrence.amount, code)
            )

            if (tx.creditCardId != null && occurrence.invoiceMonthYear != null) {
                val key = "${tx.creditCardId}_${occurrence.invoiceMonthYear}"
                if (!creditCardGroups.containsKey(key)) {
                    creditCardGroups[key] = Pair(occurrence.occurrenceDate, mutableListOf())
                }
                creditCardGroups[key]?.second?.add(uiModel)
                
                val appearance = tx.category.getAppearance()
                ccExpensesByCategory[appearance] = (ccExpensesByCategory[appearance] ?: 0.0) + occurrence.amount
            } else {
                reportItems.add(ReportItemUiModel.Transaction(uiModel, occurrence.occurrenceDate))
            }
        }

        creditCardGroups.forEach { (key, groupData) ->
            val dateSort = groupData.first
            val txs = groupData.second
            val total = txs.sumOf { it.amount }
            val cardId = txs.first().creditCardId ?: ""
            val isPaid = txs.all { it.isPaid }

            totalCcInvoices += total
            if (isPaid) paidCcInvoices += total

            val cardInfo = allCards.find { it.id == cardId }
            val displayName = cardInfo?.let { "${it.bankName} (${it.brand})" } ?: "Cartão Removido"

            reportItems.add(ReportItemUiModel.Invoice(
                cardId = cardId,
                cardName = displayName,
                monthYear = key.split("_")[1],
                totalAmount = total,
                formattedAmount = currencyManager.formatByCurrencyCode(total, code),
                isPaid = isPaid,
                transactions = txs,
                dateForSorting = dateSort
            ))
        }

        val nonCcTransactions = reportData.occurrences.filter { it.transaction.creditCardId == null }
        
        val incomeVal = nonCcTransactions
            .filter { it.transaction.direction == TransactionDirection.INCOME }
            .sumOf { it.amount }

        val nonCcExpenseVal = nonCcTransactions
            .filter { it.transaction.direction == TransactionDirection.EXPENSE }
            .sumOf { it.amount }

        val expenseVal = nonCcExpenseVal + totalCcInvoices

        val paidVal = nonCcTransactions
            .filter { it.isPaidInMonth && it.transaction.direction == TransactionDirection.EXPENSE }
            .sumOf { it.amount } + paidCcInvoices

        val pendingVal = expenseVal - paidVal
        val totalBalanceVal = incomeVal - expenseVal
        val availableBalanceVal = incomeVal - paidVal

        val expensesByCategoryMap = nonCcTransactions
            .filter { it.transaction.direction == TransactionDirection.EXPENSE }
            .groupBy { it.transaction.category.getAppearance() }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toMutableMap()

        ccExpensesByCategory.forEach { (appearance, amount) ->
            expensesByCategoryMap[appearance] = (expensesByCategoryMap[appearance] ?: 0.0) + amount
        }

        val chartLabels = expensesByCategoryMap.mapValues { entry ->
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
            chartDataValues = expensesByCategoryMap,
            chartDataLabels = chartLabels,
            items = reportItems.sortedByDescending { it.dateForSorting }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun updateMonth(newMonth: YearMonth) { selectedMonth = newMonth }

    fun toggleBalanceVisibility(isVisible: Boolean) {
        viewModelScope.launch { settingsManager.setBalanceVisible(isVisible) }
    }
}
