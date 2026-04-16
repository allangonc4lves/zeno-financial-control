package br.dev.allan.controlefinanceiro.domain.model

import androidx.compose.ui.graphics.Color

data class TransactionUIModel(
    val id: Int,
    val title: String,
    val formattedTotalAmount: String,
    val formattedAmount: String,
    val formattedParcelInfo: String?,
    val formattedDate: String,
    val color: Color,
    val category: TransactionCategory,
    val direction: TransactionDirection,
    val isPaid: Boolean,
    val isInstallment: Boolean,
    val creditCardId: String?,

)