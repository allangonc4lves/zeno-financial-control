package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.data.local.mapper.toDomain
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.utils.AddTransactionUiState
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.ValidateAmount
import br.dev.allan.controlefinanceiro.utils.ValidateCategory
import br.dev.allan.controlefinanceiro.utils.ValidateText
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.utils.parseToDouble
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val validateText: ValidateText,
    private val validateAmount: ValidateAmount,
    private val validateCategory: ValidateCategory
) {
    suspend fun execute(state: AddTransactionUiState, id: Int?): Result<Unit> {
        val titleRes = validateText.execute(state.title)
        val amountRes = validateAmount.execute(state.amount)
        val catRes = validateCategory.execute(state.category)

        if (!titleRes.successful || !amountRes.successful || !catRes.successful) {
            return Result.failure(Exception("Validation Error"))
        }

        val amount = state.amount.parseToDouble()
        // A conversão acontece aqui, usando o dado que veio do STATE
        val dateToSave = DateHelper.fromUiToDb(state.dateDisplay)

        return try {
            if (id == null) {
                if (state.transactionType == TransactionType.INSTALLMENT) {
                    // Passamos a data convertida para a função de parcelas
                    repository.insertTransactions(generateInstallments(state, amount, dateToSave))
                } else {
                    repository.insertTransaction(state.toDomain(amount, dateToSave))
                }
            } else {
                repository.updateTransaction(state.toDomain(amount, dateToSave, id))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateInstallments(
        state: AddTransactionUiState,
        total: Double,
        baseDate: String
    ): List<Transaction> {
        val groupId = java.util.UUID.randomUUID().toString()

        return (0 until state.installmentCount).map { i ->
            val calendar = Calendar.getInstance().apply {
                val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(baseDate)
                time = dateObj ?: Date()
                add(Calendar.MONTH, i)
            }

            // Converte a data da parcela atual de volta para String do banco
            val installmentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

            state.toDomain(total / state.installmentCount, installmentDate).copy(
                groupId = groupId,
                currentInstallment = i + 1,
                isPaid = i == 0 && state.isPaid,
                isInstallment = true
            )
        }
    }
}

private fun AddTransactionUiState.toDomain(amount: Double, dateForDb: String, id: Int = 0) = Transaction(
    id = id,
    title = this.title,
    amount = amount,
    date = dateForDb,
    category = this.category!!,
    direction = this.direction,
    creditCardId = this.selectedCardId,
    isPaid = this.isPaid,
    type = this.transactionType,
    installmentCount = if(this.transactionType == TransactionType.INSTALLMENT) this.installmentCount else 0
)