package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
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
                // Transações expandidas (possuem índice de parcela/repetição)
                // Devem aparecer APENAS no mês exato da sua data
                tx.currentInstallment > 0 -> txYearMonth == month

                // Transações do tipo REPEAT que não foram expandidas (templates ou legadas)
                tx.type == TransactionType.REPEAT -> {
                    if (tx.installmentCount > 1) {
                        // Repetição com limite (comportamento de parcelas projetadas)
                        val monthsBetween = ChronoUnit.MONTHS.between(txYearMonth.atDay(1), month.atDay(1)).toInt()
                        monthsBetween in 0 until tx.installmentCount
                    } else {
                        // Repetição infinita (aparece em todos os meses após o início)
                        !month.isBefore(txYearMonth)
                    }
                }

                // Parcelamentos legados (isInstallment antigo sem type REPEAT)
                tx.isInstallment && tx.installmentCount > 1 -> {
                    val monthsBetween = ChronoUnit.MONTHS.between(txYearMonth.atDay(1), month.atDay(1)).toInt()
                    monthsBetween in 0 until tx.installmentCount
                }
                
                // Transações comuns (DEFAULT)
                else -> txYearMonth == month
            }
        }
    }

    fun getAmountForMonth(tx: Transaction): Double {
        // Se for um registro legado ou template que precisa de divisão de valor
        return if (tx.currentInstallment == 0 && tx.isInstallment && tx.installmentCount > 1 && tx.type != TransactionType.REPEAT) {
            (tx.amount / tx.installmentCount).round2()
        } else {
            tx.amount
        }
    }

    private fun Double.round2() = Math.round(this * 100.0) / 100.0
}