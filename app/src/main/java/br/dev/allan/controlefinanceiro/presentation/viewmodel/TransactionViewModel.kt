package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    val transactions = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted
        .WhileSubscribed(5000), emptyList())

    fun addTransaction(
        title: String,
        amount: Double,
        date: Long,
        category: String,
        iconResId: Int,
        isFixed: Boolean,
        isInstallment: Boolean,
        installmentCount: Int,
        type: TransactionINorEX
    ) {
        viewModelScope.launch {
            val cleanIsFixed = if (type == TransactionINorEX.EXPENSE) isFixed else false
            val cleanIsInstallment = if (type == TransactionINorEX.EXPENSE) isInstallment else false
            val cleanInstallmentCount = if (type == TransactionINorEX.EXPENSE) installmentCount else 0

            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    date = date,
                    category = category,
                    iconResId = iconResId,
                    isFixed = cleanIsFixed,
                    isInstallment = cleanIsInstallment,
                    installmentCount = cleanInstallmentCount,
                    type = type
                )
            )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.updateTransaction(transaction) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}