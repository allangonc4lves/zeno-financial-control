package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportFilterState
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportItemUiModel
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportUiState
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
import br.dev.allan.controlefinanceiro.data.local.mapper.toUi
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.usecase.GetReportUseCase
import br.dev.allan.controlefinanceiro.domain.usecase.ReportData
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TransactionTypeFilter { ALL, INCOME, EXPENSE, INVOICES_ONLY, WALLET_ONLY }
enum class TransactionStatusFilter { ALL, PAID, UNPAID }

fun Double.round2() = Math.round(this * 100.0) / 100.0

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val getReportUseCase: GetReportUseCase,
    private val settingsManager: SettingsManager,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val _filterState = MutableStateFlow(getDefaultMonthRange())
    val filterState = _filterState.asStateFlow()

    private val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    val isBalanceVisible = settingsManager.isBalanceVisible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportUiState = combine(
        _filterState,
        currencyCode,
        transactionRepository.getAllPaymentStatuses(),
        cardRepository.getCards(),
        transactionRepository.getTransactions()
    ) { filters, code, payments, cards, allTransactions ->
        val reportData = getReportUseCase(allTransactions, filters, payments, cards)
        mapToUiState(reportData, filters, code, cards)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState(isLoading = true))

    private fun mapToUiState(
        data: ReportData,
        filters: ReportFilterState,
        code: String,
        cards: List<CreditCard>
    ): ReportUiState {
        val reportItems = mutableListOf<ReportItemUiModel>()
        val creditCardGroups = mutableMapOf<String, Pair<Long, MutableList<TransactionUIState>>>()

        data.occurrences.forEach { occurrence ->
            val tx = occurrence.transaction
            val occurrenceDate = Date(occurrence.occurrenceDate)
            val formattedDateOccur = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(occurrenceDate)
            
            val uiModel = tx.toUi(currencyManager, code).copy(
                amount = occurrence.amount,
                dateMillis = occurrence.occurrenceDate,
                formattedDate = formattedDateOccur,
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
            } else {
                reportItems.add(ReportItemUiModel.Transaction(uiModel, occurrence.occurrenceDate))
            }
        }

        creditCardGroups.forEach { (key, groupData) ->
            val dateSort = groupData.first
            val txs = groupData.second
            val total = txs.sumOf { it.amount }
            val cardId = txs.first().creditCardId ?: ""

            val cardInfo = cards.find { it.id == cardId }
            val displayName = cardInfo?.let { "${it.bankName} (${it.brand})" } ?: "Cartão Removido"

            reportItems.add(ReportItemUiModel.Invoice(
                cardId = cardId,
                cardName = displayName,
                monthYear = key.split("_")[1],
                totalAmount = total,
                formattedAmount = currencyManager.formatByCurrencyCode(total, code),
                isPaid = txs.all { it.isPaid },
                transactions = txs,
                dateForSorting = dateSort
            ))
        }

        return ReportUiState(
            items = reportItems.sortedByDescending { it.dateForSorting },
            formattedTotalIncome = currencyManager.formatByCurrencyCode(data.totalIncome, code),
            formattedTotalExpense = currencyManager.formatByCurrencyCode(data.totalExpense, code),
            formattedBalance = currencyManager.formatByCurrencyCode(data.totalIncome - data.totalExpense, code),
            isLoading = false
        )
    }

    fun updateTypeFilter(type: TransactionTypeFilter) { _filterState.update { it.copy(typeFilter = type) } }
    fun updateStatusFilter(status: TransactionStatusFilter) { _filterState.update { it.copy(statusFilter = status) } }
    fun updateDateRange(start: Long, end: Long) { _filterState.update { it.copy(startDate = start, endDate = end) } }

    private fun getDefaultMonthRange(): ReportFilterState {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return ReportFilterState(startDate = start, endDate = cal.timeInMillis)
    }

    fun updateCategoryFilter(categoryName: String?) {
        _filterState.update { it.copy(categoryFilter = categoryName) }
    }
}
