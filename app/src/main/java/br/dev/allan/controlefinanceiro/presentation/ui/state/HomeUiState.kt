package br.dev.allan.controlefinanceiro.presentation.ui.state

import br.dev.allan.controlefinanceiro.domain.model.CategoryAppearance

data class HomeUiState(
    val balance: String = "...",
    val rawBalance: Double = 0.0,
    val availableBalance: String = "...",
    val rawAvailableBalance: Double = 0.0,
    val incomes: String = "...",
    val expenses: String = "...",
    val paidValue: String = "...",
    val pendingValue: String = "...",
    val isBalanceVisible: Boolean = true,
    val chartDataValues: Map<CategoryAppearance, Double> = emptyMap(),
    val chartDataLabels: Map<CategoryAppearance, String> = emptyMap(),
    val items: List<ReportItemUiModel> = emptyList()
)
