package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.domain.repository.CreditCardRepository
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateAmount
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateCategory
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateText
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.AddTransactionUiState
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.text.replace

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val validateText: ValidateText = ValidateText(),
    private val validateAmount: ValidateAmount = ValidateAmount(),
    private val validateCategory: ValidateCategory = ValidateCategory(),
) : ViewModel() {

    private var currentTransactionId: Int? = null
    var uiState by mutableStateOf(AddTransactionUiState())
        private set

    private val _uiEvent = Channel<SaveTransactionUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            creditCardRepository.getCards().collect { cards ->
                uiState = uiState.copy(cards = cards)
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        uiState = uiState.copy(title = newTitle, titleError = null)

    }

    fun onAmountChange(newAmount: String) {
        var digitsOnly = newAmount.filter { it.isDigit() }

        if (digitsOnly.length > 9) {
            digitsOnly = digitsOnly.take(9)
        }

        val doubleValue = digitsOnly.toDoubleOrNull()?.div(100) ?: 0.0
        val symbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            currencySymbol = ""
            decimalSeparator = ','
            groupingSeparator = '.'
        }

        val formatter = DecimalFormat("#,##0.00", symbols)
        val formatted = formatter.format(doubleValue).trim()

        uiState = uiState.copy(amount = formatted)
    }

    fun onCategoryChange(category: TransactionCategory) {
        uiState = uiState.copy(
            category = category,
            categoryError = null,
            selectedCardId = if (category == TransactionCategory.CREDIT_CARD_PAYMENT) uiState.selectedCardId else null
        )
        Log.i("onCategoryChange", uiState.selectedCardId.toString())
    }

    fun onSelectCard(cardId: String?) {
        uiState = uiState.copy(selectedCardId = cardId)
        Log.i("onSelectCard", cardId.toString())
    }

    fun onInstallmentCountChange(count: Int) {
        val validCount = if (count in 2..360) count else 2
        uiState = uiState.copy(installmentCount = validCount)
    }

    fun onDateChange(millis: Long) {
        uiState = uiState.copy(dateMillis = millis)
    }

    fun onDirectionChange(direction: TransactionDirection) {
        uiState = uiState.copy(direction = direction, category = null)
    }

    fun onTransactionTypeChange(type: TransactionType) {
        uiState = uiState.copy(transactionType = type)
    }

    fun onPaidChange(paid: Boolean) {
        uiState = uiState.copy(isPaid = paid)
    }

    fun togglePaymentStatus(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.isInstallment) {

                transactionRepository.incrementPaidInstallment(transaction.id)
            } else {

                transactionRepository.updatePaymentStatus(transaction.id, !transaction.isPaid)
            }
        }
    }

    fun deleteTransaction() {
        val idToDelete = currentTransactionId ?: return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            transactionRepository.deleteTransaction(idToDelete)

            _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun loadTransactionToEdit(id: Int) {
        viewModelScope.launch {
            transactionRepository.getTransactionById(id)?.let { tx ->
                currentTransactionId = tx.id

                uiState = uiState.copy(
                    title = tx.title,
                    amount = formatAmountForUi(tx.amount),
                    dateMillis = tx.date,
                    category = tx.category,
                    direction = tx.direction,
                    isPaid = tx.isPaid,
                    selectedCardId = tx.creditCardId,
                    transactionType = when {
                        tx.isFixed -> TransactionType.FIXED
                        tx.isInstallment -> TransactionType.INSTALLMENT
                        else -> TransactionType.DEFAULT
                    },
                    installmentCount = if (tx.isInstallment) tx.installmentCount else 2
                )
            }
        }
    }

    fun saveTransaction() {
        uiState = uiState.copy(isLoading = true)

        val titleResult = validateText.execute(uiState.title)
        val amountResult = validateAmount.execute(uiState.amount)
        val categoryResult = validateCategory.execute(uiState.category)

        if (uiState.category == TransactionCategory.CREDIT_CARD_PAYMENT && uiState.selectedCardId.isNullOrBlank()) {
            uiState = uiState.copy(isLoading = false, categoryError = "Selecione um cartão")
            return
        }

        val hasError = listOf(titleResult, amountResult, categoryResult).any { !it.successful }

        if (hasError) {
            uiState = uiState.copy(
                isLoading = false,
                titleError = titleResult.errorMessage,
                amountError = amountResult.errorMessage,
                categoryError = categoryResult.errorMessage,
            )
            return
        }

        val amountToSave = parseAmountFromUi(uiState.amount)

        val transaction = Transaction(
            id = currentTransactionId ?: 0,
            title = uiState.title,
            amount = amountToSave,
            date = uiState.dateMillis,
            category = uiState.category!!,
            isFixed = uiState.transactionType == TransactionType.FIXED,
            isInstallment = uiState.transactionType == TransactionType.INSTALLMENT,
            installmentCount = if (uiState.transactionType == TransactionType.INSTALLMENT) uiState.installmentCount else 0,
            direction = uiState.direction,
            creditCardId = uiState.selectedCardId,
            isPaid = uiState.isPaid,
            paidInstallments = uiState.paidInstallments
        )

        viewModelScope.launch {
            if (currentTransactionId == null) {
                transactionRepository.insertTransaction(transaction)
            } else {
                transactionRepository.updateTransaction(transaction)
            }

            _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun resetState() {
        currentTransactionId = null
        uiState = AddTransactionUiState()
    }

    private fun formatAmountForUi(amount: Double): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
        return formatter.format(amount)
    }

    private fun parseAmountFromUi(amountStr: String): Double {
        return amountStr
            .replace(Regex("[^0-9,]"), "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0
    }
}