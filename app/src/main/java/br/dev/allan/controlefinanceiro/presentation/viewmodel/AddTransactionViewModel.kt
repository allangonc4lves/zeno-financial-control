package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.local.mapper.toEntity
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.text.replace

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val validateText: ValidateText = ValidateText(),
    private val validateAmount: ValidateAmount = ValidateAmount(),
    private val validateCategory: ValidateCategory = ValidateCategory(),
) : ViewModel() {

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

    fun saveTransaction() {
        uiState = uiState.copy(isLoading = true)

        val titleResult = validateText.execute(uiState.title)
        val amountResult = validateAmount.execute(uiState.amount)
        val categoryResult = validateCategory.execute(uiState.category)

        val amountToSave = uiState.amount
            .replace("R$", "")
            .replace(Regex("[\\s.]"), "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

        if (uiState.category == TransactionCategory.CREDIT_CARD_PAYMENT && uiState.selectedCardId.isNullOrBlank()) {
            uiState = uiState.copy(
                isLoading = false,
                categoryError = "Selecione um cartão para continuar"
            )
            return
        }

        val hasError = listOf(titleResult, amountResult, categoryResult).any { !it.successful }

        if (hasError) {
            uiState = uiState.copy(isLoading = false)
            uiState = uiState.copy(
                titleError = titleResult.errorMessage,
                amountError = amountResult.errorMessage,
                categoryError = categoryResult.errorMessage,
            )
            return
        } else {
            val transaction = Transaction(
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
            Log.i("SaveCard", uiState.selectedCardId.toString())
            viewModelScope.launch {
                transactionRepository.insertTransaction(transaction)
                delay(2000L)
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}