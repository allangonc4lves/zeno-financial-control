package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.text.format.DateFormat
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.util.CurrencyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MonthTransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)

    val currentMonth = _currentMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionsUiState: StateFlow<List<TransactionUIModel>> = _currentMonth
        .flatMapLatest { monthMillis ->
            val (start, end) = getMonthRange(monthMillis)
            repository.getTransactionsByMonth(start, end).map { list ->
                list.filter { transaction ->
                    when {
                        transaction.date > end -> false

                        transaction.isInstallment -> !transaction.isExpired(monthMillis)

                        else -> true
                    }
                }.map { transaction ->
                    val titleWithParcel = transaction.getDisplayTitle(monthMillis)

                    val datePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM")
                    TransactionUIModel(
                        id = transaction.id,
                        title = titleWithParcel,
                        formattedAmount = currencyManager.formatByCurrencyCode(transaction.amount, "BRL"),
                        formattedDate = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(transaction.date)),
                        color = if (transaction.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        category = transaction.category,
                        direction = transaction.direction,
                        isPaid = transaction.isPaid,
                        isInstallment = transaction.isInstallment,
                        creditCardId = transaction.creditCardId,
                        formattedParcelInfo = null,
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(transaction.amount, "BRL"),
                        amount = transaction.amount
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun changeMonth(increment: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _currentMonth.value }
        cal.add(Calendar.MONTH, increment)
        _currentMonth.value = cal.timeInMillis
    }

    fun togglePayment(uiModel: TransactionUIModel) {
        viewModelScope.launch {
            if (uiModel.isInstallment) {
                repository.incrementPaidInstallment(uiModel.id)
            } else {
                repository.updatePaymentStatus(uiModel.id, !uiModel.isPaid)
            }
        }
    }
    private fun getMonthRange(monthMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = monthMillis }
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}