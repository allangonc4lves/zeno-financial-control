package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    val transactions = repository.getExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(title: String, amount: Double, date: Long) {
        viewModelScope.launch {
            repository.addExpense(Transaction(title = title, amount = amount, date = date))
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.updateExpense(transaction) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteExpense(transaction) }
    }
}