package br.dev.allan.controlefinanceiro.utils

import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AddTransactionUiState(
    val title: String = "",
    val groupId: String? = null,
    val amount: String = "",
    val dateDisplay: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val dateMillis: Long = System.currentTimeMillis(),
    val direction: TransactionDirection = TransactionDirection.EXPENSE,
    val category: TransactionCategory? = null,
    val transactionType: TransactionType = TransactionType.DEFAULT,
    val installmentCount: Int = 2,
    val currentInstallment: Int = 0,
    val isDatePickerVisible: Boolean = false,
    val isLoading: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val installmentCountError: String? = null,
    val selectedCardId: String? = null,
    val cards: List<CreditCard> = emptyList(),
    val isPaid: Boolean = false,
    val paidInstallments: Int = 0,
    val isCreditCard: Boolean = false
)