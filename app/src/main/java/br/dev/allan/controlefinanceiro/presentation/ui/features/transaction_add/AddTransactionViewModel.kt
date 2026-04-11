package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    var uiState by mutableStateOf(TransactionUiState())
        private set

    fun onTitleChange(newTitle: String) { uiState = uiState.copy(title = newTitle) }
    fun onAmountChange(newAmount: String) { uiState = uiState.copy(amount = newAmount) }
    fun onDateChange(millis: Long) { uiState = uiState.copy(dateMillis = millis) }

    fun onDirectionChange(direction: TransactionDirection) {
        uiState = uiState.copy(direction = direction, category = null)
    }

    fun onTransactionTypeChange(type: TransactionType) {
        uiState = uiState.copy(transactionType = type)
    }

    fun onInstallmentCountChange(count: Int) {
        uiState = uiState.copy(installmentCount = count)
    }

    fun onCategoryChange(category: TransactionCategory) {
        uiState = uiState.copy(category = category)
    }

    fun saveTransaction() {
        val current = uiState
        val transaction = Transaction(
            title = current.title,
            amount = current.amount.toDoubleOrNull() ?: 0.0,
            date = current.dateMillis,
            category = current.category ?: TransactionCategory.OTHERS_EXPENSE,
            isFixed = current.transactionType == TransactionType.FIXED,
            isInstallment = current.transactionType == TransactionType.INSTALLMENT,
            installmentCount = if (current.transactionType == TransactionType.INSTALLMENT) current.installmentCount else 0,
            type = current.direction
        )
        viewModelScope.launch { repository.insertTransaction(transaction) }
    }
}