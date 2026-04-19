package br.dev.allan.controlefinanceiro.domain.model

import androidx.compose.ui.graphics.Color

data class TransactionUIModel(
    val id: Int,
    val groupId: String? = null,
    val title: String,
    val amount: Double,
    val formattedTotalAmount: String,
    val formattedAmount: String,
    val formattedParcelInfo: String?,
    val formattedDate: String,
    val color: Color,
    val category: TransactionCategory,
    val type: TransactionType,
    val direction: TransactionDirection,
    val isPaid: Boolean,
    val isFixed: Boolean,
    val isInstallment: Boolean,
    val currentInstallment: Int = 0,
    val creditCardId: String?,
)