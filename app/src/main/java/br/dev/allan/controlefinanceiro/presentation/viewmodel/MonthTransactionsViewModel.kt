package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.text.format.DateFormat
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
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
    val transactionsUiModel: StateFlow<List<TransactionUIModel>> = _currentMonth
        .flatMapLatest { monthMillis ->
            val (start, end) = getMonthRange(monthMillis)
            val startStr = DateHelper.fromMillisToDb(start)
            val endStr = DateHelper.fromMillisToDb(end)

            repository.getTransactionsByMonth(start, end).map { list ->
                list.filter { transaction ->
                    when {
                        transaction.date > endStr -> false

                        transaction.isInstallment -> !transaction.isExpired(monthMillis)

                        else -> true
                    }
                }.map { transaction ->
                    val datePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM")

                    val dateForUi = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(transaction.date) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    TransactionUIModel(
                        id = transaction.id,
                        title = transaction.title,
                        amount = transaction.amount,
                        formattedTotalAmount = currencyManager.formatByCurrencyCode(transaction.amount, "BRL"),
                        formattedAmount = currencyManager.formatByCurrencyCode(transaction.amount, "BRL"),
                        formattedParcelInfo = null,
                        formattedDate = SimpleDateFormat(datePattern, Locale.getDefault()).format(dateForUi),
                        color = if (transaction.direction == TransactionDirection.EXPENSE) Color.Red else Color.Green,
                        category = transaction.category,
                        type = transaction.type,
                        direction = transaction.direction,
                        isPaid = transaction.isPaid,
                        isInstallment = transaction.isInstallment,
                        currentInstallment = transaction.currentInstallment,
                        installmentCount = transaction.installmentCount,
                        creditCardId = transaction.creditCardId,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun togglePayment(uiModel: TransactionUIModel) {
        viewModelScope.launch {
            val id = uiModel.id

            if (uiModel.type == TransactionType.DEFAULT) {
                val monthYear = formatMillisToMonthYear(currentMonth.value)

                if (uiModel.isPaid) {
                    repository.markAsUnpaid(id.toString(), monthYear)
                } else {
                    repository.markAsPaid(id.toString(), monthYear)
                }
            } else {
                repository.updatePaymentStatus(id, !uiModel.isPaid)
            }
        }
    }

    fun changeMonth(increment: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _currentMonth.value }
        cal.add(Calendar.MONTH, increment)
        _currentMonth.value = cal.timeInMillis
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

