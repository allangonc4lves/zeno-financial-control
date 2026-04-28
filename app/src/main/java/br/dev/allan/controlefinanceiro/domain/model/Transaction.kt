package br.dev.allan.controlefinanceiro.domain.model

import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val groupId: String? = null,
    val title: String,
    val amount: Double,
    val date: String,
    val category: TransactionCategory,
    val type: TransactionType,
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val currentInstallment: Int = 0,
    val isPaid: Boolean = false,
    val direction: TransactionDirection,
    val creditCardId: String? = null
){
    fun getInvoiceMonthStart(closingDay: Int): Long {
        val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val transactionDate = try {
            dbFormat.parse(this.date) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply {
            time = transactionDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.get(Calendar.DAY_OF_MONTH) >= closingDay) {
            cal.add(Calendar.MONTH, 1)
        }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis
    }

    fun getParcelIndexForInvoiceMonth(referenceMonthMillis: Long, closingDay: Int): Int {
        if (!isInstallment && type != TransactionType.REPEAT) return 1

        val firstInvoiceMonthStart = getInvoiceMonthStart(closingDay)
        
        val refCal = Calendar.getInstance().apply {
            timeInMillis = referenceMonthMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startCal = Calendar.getInstance().apply {
            timeInMillis = firstInvoiceMonthStart
        }

        val yearDiff = refCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        val monthDiff = refCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
        val monthsBetween = (yearDiff * 12) + monthDiff

        return (monthsBetween + 1)
    }

    private fun getMonthsBetween(referenceDate: Long): Int {
        val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val transactionDate = try {
            dbFormat.parse(this.date) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        
        val startCal = Calendar.getInstance().apply {
            time = transactionDate
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val refCal = Calendar.getInstance().apply {
            timeInMillis = referenceDate
            set(Calendar.DAY_OF_MONTH, 1)
        }
        
        val yearDiff = refCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        val monthDiff = refCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)

        return (yearDiff * 12) + monthDiff
    }
    fun getCurrentParcelIndex(referenceDate: Long): Int {
        if (!isInstallment && type != TransactionType.REPEAT) return 1

        val monthsBetween = getMonthsBetween(referenceDate)
        
        return (monthsBetween + 1).coerceIn(1, installmentCount)
    }

    fun isExpired(referenceDate: Long): Boolean {
        if (!isInstallment) return false

        val monthsBetween = getMonthsBetween(referenceDate)
        
        return monthsBetween >= installmentCount
    }
}

