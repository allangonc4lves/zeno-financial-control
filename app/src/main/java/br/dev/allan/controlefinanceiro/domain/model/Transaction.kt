package br.dev.allan.controlefinanceiro.domain.model

import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import java.util.Calendar

data class Transaction(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: TransactionCategory,
    val isFixed: Boolean = false,
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val paidInstallments: Int = 0,
    val isPaid: Boolean = false,
    val direction: TransactionDirection,
    val creditCardId: String? = null
){
    fun getCurrentParcelIndex(referenceDate: Long): Int {
        if (!isInstallment) return 1

        val startCal = Calendar.getInstance().apply { timeInMillis = date }
        val refCal = Calendar.getInstance().apply { timeInMillis = referenceDate }

        val yearDiff = refCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        val monthDiff = refCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)

        val monthsBetween = (yearDiff * 12) + monthDiff

        return (monthsBetween + 1).coerceIn(1, installmentCount)
    }

    fun getDisplayTitle(referenceDate: Long): String {
        if (!isInstallment) return title
        val current = getCurrentParcelIndex(referenceDate)
        return "$title ($current/$installmentCount)"
    }

    fun isExpired(referenceDate: Long): Boolean {
        if (!isInstallment) return false

        val startCal = Calendar.getInstance().apply { timeInMillis = date }
        val refCal = Calendar.getInstance().apply { timeInMillis = referenceDate }

        val yearDiff = refCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        val monthDiff = refCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)

        val monthsBetween = (yearDiff * 12) + monthDiff

        return monthsBetween >= installmentCount
    }
}

