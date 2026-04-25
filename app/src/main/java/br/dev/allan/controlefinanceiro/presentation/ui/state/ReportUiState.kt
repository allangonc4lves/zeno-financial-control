package br.dev.allan.controlefinanceiro.presentation.ui.state

import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionStatusFilter
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionTypeFilter

data class ReportFilterState(
    val startDate: Long,
    val endDate: Long,
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val statusFilter: TransactionStatusFilter = TransactionStatusFilter.ALL,
    val categoryFilter: String? = null
)

data class ReportUiState(
    val items: List<ReportItemUiModel> = emptyList(),
    val formattedTotalIncome: String = "",
    val formattedTotalExpense: String = "",
    val formattedBalance: String = "",
    val isLoading: Boolean = false
)

sealed class ReportItemUiModel {
    abstract val dateForSorting: Long

    data class Transaction(
        val model: TransactionUIState,
        override val dateForSorting: Long
    ) : ReportItemUiModel()

    data class Invoice(
        val cardId: String,
        val cardName: String,
        val monthYear: String,
        val totalAmount: Double,
        val formattedAmount: String,
        val isPaid: Boolean,
        val transactions: List<TransactionUIState>,
        override val dateForSorting: Long
    ) : ReportItemUiModel()
}
