package br.dev.allan.controlefinanceiro.utils

import androidx.compose.ui.graphics.Color
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransactionUIModel(
    val id: Int,
    val groupId: String? = null,
    val title: String,
    val amount: Double,
    val dateDisplay: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val formattedTotalAmount: String,
    val formattedAmount: String,
    val formattedParcelInfo: String?,
    val formattedDate: String,
    val color: Color,
    val category: TransactionCategory,
    val type: TransactionType,
    val direction: TransactionDirection,
    val isPaid: Boolean,
    val isInstallment: Boolean,
    val currentInstallment: Int = 0,
    val creditCardId: String?,
    val installmentCount: Int,
)