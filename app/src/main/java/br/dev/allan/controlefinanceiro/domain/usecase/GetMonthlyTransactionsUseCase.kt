package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.constants.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetMonthlyTransactionsUseCase @Inject constructor() {

    operator fun invoke(allTransactions: List<Transaction>, month: YearMonth): List<Transaction> {
        return allTransactions.filter { tx ->
            val txDate = try {
                LocalDate.parse(tx.date)
            } catch (e: Exception) {
                null
            } ?: return@filter false
            
            val txYearMonth = YearMonth.from(txDate)
            
            when {
                tx.currentInstallment > 0 -> txYearMonth == month

                tx.type == TransactionType.REPEAT -> {
                    if (tx.installmentCount > 1) {
                        val monthsBetween = ChronoUnit.MONTHS.between(txYearMonth.atDay(1), month.atDay(1)).toInt()
                        monthsBetween in 0 until tx.installmentCount
                    } else {
                        !month.isBefore(txYearMonth)
                    }
                }

                tx.isInstallment && tx.installmentCount > 1 -> {
                    val monthsBetween = ChronoUnit.MONTHS.between(txYearMonth.atDay(1), month.atDay(1)).toInt()
                    monthsBetween in 0 until tx.installmentCount
                }

                else -> txYearMonth == month
            }
        }
    }

    fun getAmountForMonth(tx: Transaction): Double {
        return if (tx.currentInstallment == 0 && tx.isInstallment && tx.installmentCount > 1 && tx.type != TransactionType.REPEAT) {
            (tx.amount / tx.installmentCount).round2()
        } else {
            tx.amount
        }
    }

    private fun Double.round2() = Math.round(this * 100.0) / 100.0
}