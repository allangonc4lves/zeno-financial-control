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
    private fun getMonthsBetween(referenceDate: Long): Int {
        // 1. Converter a String do banco (yyyy-MM-dd) para Date
        val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val transactionDate = try {
            dbFormat.parse(this.date) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        // 2. Configurar os Calendars
        val startCal = Calendar.getInstance().apply { 
            time = transactionDate 
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val refCal = Calendar.getInstance().apply { 
            timeInMillis = referenceDate 
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // 3. Calcular a diferença
        val yearDiff = refCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        val monthDiff = refCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)

        return (yearDiff * 12) + monthDiff
    }
    fun getCurrentParcelIndex(referenceDate: Long): Int {
        if (!isInstallment && type != TransactionType.REPEAT) return 1

        val monthsBetween = getMonthsBetween(referenceDate)

        // Retorna o índice da parcela (1, 2, 3...), limitado ao total de parcelas
        return (monthsBetween + 1).coerceIn(1, installmentCount)
    }

    fun isExpired(referenceDate: Long): Boolean {
        if (!isInstallment) return false

        val monthsBetween = getMonthsBetween(referenceDate)

        // Se a diferença de meses for igual ou maior que o total de parcelas, expirou
        // Ex: Compra em 3x. Meses entre: 0 (parc 1), 1 (parc 2), 2 (parc 3). Se >= 3, expirou.
        return monthsBetween >= installmentCount
    }
}

