package br.dev.allan.controlefinanceiro.presentation.ui.state

import androidx.compose.ui.graphics.Color
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.constants.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransactionUIState(
    val id: String = "",
    val groupId: String? = null,
    val title: String = "",
    val amount: Double = 0.0,
    val amountInput: String = "",
    val dateDisplay: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val dateMillis: Long = System.currentTimeMillis(),
    val formattedTotalAmount: String = "",
    val formattedAmount: String = "",
    val formattedParcelInfo: String? = null,
    val formattedDate: String = "",
    val color: Color = Color.Unspecified,
    val category: TransactionCategory? = null,
    val type: TransactionType = TransactionType.DEFAULT,
    val direction: TransactionDirection = TransactionDirection.EXPENSE,
    val isPaid: Boolean = false,
    val isInstallment: Boolean = false,
    val currentInstallment: Int = 0,
    val creditCardId: String? = null,
    val installmentCount: Int = 1,
    val isDivideValue: Boolean = true,
    val isDatePickerVisible: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val installmentCountError: String? = null,
    val cards: List<CreditCard> = emptyList(),
    val isCreditCard: Boolean = false
)
