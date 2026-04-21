package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetMonthlyTransactionsUseCase @Inject constructor() {

    operator fun invoke(allTransactions: List<Transaction>, month: YearMonth): List<Transaction> {
        val (start, end) = getMonthRange(month)
        return allTransactions.filter { isTransactionInMonth(it, month, start, end) }
    }
    fun getAmountForMonth(tx: Transaction): Double {
        return if (tx.isInstallment && tx.installmentCount > 0) {
            (tx.amount / tx.installmentCount).round2()
        } else {
            tx.amount
        }
    }

    private fun getMonthRange(month: YearMonth): Pair<Long, Long> {
        val start = month.atDay(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    private fun isTransactionInMonth(tx: Transaction, month: YearMonth, start: Long, end: Long): Boolean {
        val txDate = try {
            LocalDate.parse(tx.date)
        } catch (e: Exception) {
            LocalDate.now()
        }

        return when {
            tx.isInstallment -> {
                val monthsBetween = ChronoUnit.MONTHS.between(
                    YearMonth.from(txDate).atDay(1),
                    month.atDay(1)
                ).toInt()

                monthsBetween in 0 until tx.installmentCount
            }
            else -> {
                YearMonth.from(txDate) == month
            }
        }
    }

    private fun Double.round2() = Math.round(this * 100.0) / 100.0
}