package br.dev.allan.controlefinanceiro.utils

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
    val transactions: List<TransactionUIModel> = emptyList(),
    val chartDataValues: Map<CategoryAppearance, Double> = emptyMap(),
    val chartDataLabels: Map<CategoryAppearance, String> = emptyMap()
)