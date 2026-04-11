package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add

import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType

data class TransactionUiState(
    val title: String = "",
    val amount: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val direction: TransactionDirection = TransactionDirection.EXPENSE,
    val category: TransactionCategory? = null,
    val transactionType: TransactionType = TransactionType.DEFAULT,
    val installmentCount: Int = 2,
    val isDatePickerVisible: Boolean = false
)
