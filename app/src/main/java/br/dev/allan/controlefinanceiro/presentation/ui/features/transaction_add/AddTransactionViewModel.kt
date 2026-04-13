package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateAmount
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateCategory
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val validateTitle: ValidateTitle = ValidateTitle(),
    private val validateAmount: ValidateAmount = ValidateAmount(),
    private val validateCategory: ValidateCategory = ValidateCategory(),
) : ViewModel() {

    var uiState by mutableStateOf(AddTransactionUiState())
        private set

    private val _uiEvent = Channel<SaveTransactionUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onTitleChange(newTitle: String) {
        uiState = uiState.copy(title = newTitle, titleError = null)

    }

    fun onAmountChange(newAmount: String) {
        val digitsOnly = newAmount.filter { it.isDigit() }

        if (digitsOnly.isEmpty()) {
            uiState = uiState.copy(amount = "", amountError = null)
            return
        }

        if (digitsOnly.length <= 9) {
            val formatted = formatToCurrency(digitsOnly)
            uiState = uiState.copy(amount = formatted, amountError = null)
        }
    }

    private fun formatToCurrency(digits: String): String {
        val doubleValue = digits.toLong().toDouble() / 100
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatter.format(doubleValue)
    }

    fun onCategoryChange(category: TransactionCategory) {
        uiState = uiState.copy(category = category, categoryError = null)
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

    fun saveTransaction() {
        uiState = uiState.copy(isLoading = true)

        val titleResult = validateTitle.execute(uiState.title)
        val amountResult = validateAmount.execute(uiState.amount)
        val categoryResult = validateCategory.execute(uiState.category)

        val amountToSave = uiState.amount
            .replace("R$", "")
            .replace(Regex("[\\s.]"), "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

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
                type = uiState.direction
            )
            viewModelScope.launch {
                repository.insertTransaction(transaction)
                delay(2000L)
                _uiEvent.send(SaveTransactionUiEvent.SaveSuccess)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}
