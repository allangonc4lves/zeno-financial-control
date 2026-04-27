package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.repository.TransactionRepository
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
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
    val validateText: ValidateText,
    val validateAmount: ValidateAmount,
    val validateCategory: ValidateCategory
) {
    suspend fun execute(state: TransactionUIState, id: String?, editAll: Boolean = false): Result<Unit> {
        val titleRes = validateText.execute(state.title)
        val amountRes = validateAmount.execute(state.amountInput)
        val catRes = validateCategory.execute(state.category)

        if (!titleRes.successful || !amountRes.successful || !catRes.successful) {
            return Result.failure(Exception("validation_error"))
        }

        val amount = state.amountInput.parseToDouble()
        val dateToSave = DateHelper.fromUiToDb(state.dateDisplay)

        return try {
            if (id == null) {
                if (state.type == TransactionType.REPEAT) {
                    repository.insertTransactions(generateInstallments(state, amount, dateToSave))
                } else {
                    repository.insertTransaction(state.toDomain(amount, dateToSave))
                }
            } else {
                if (editAll && state.groupId != null) {
                    // Lógica para atualizar todas as parcelas do grupo
                    repository.updateTransactionGroup(
                        groupId = state.groupId,
                        title = state.title,
                        amount = amount,
                        category = state.category!!,
                        creditCardId = state.creditCardId
                    )
                } else {
                    repository.updateTransaction(state.toDomain(amount, dateToSave, id))
                    if (state.groupId != null) {
                        repository.updateCardIdByGroupId(state.groupId, state.creditCardId)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateInstallments(
        state: TransactionUIState,
        total: Double,
        baseDate: String
    ): List<Transaction> {
        val groupId = java.util.UUID.randomUUID().toString()
        val installmentAmount = if (state.isDivideValue) (total / state.installmentCount) else total

        return (0 until state.installmentCount).map { i ->
            val calendar = Calendar.getInstance().apply {
                val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(baseDate)
                time = dateObj ?: Date()
                add(Calendar.MONTH, i)
            }

            val installmentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

            state.toDomain(installmentAmount, installmentDate).copy(
                groupId = groupId,
                currentInstallment = i + 1,
                installmentCount = state.installmentCount,
                isPaid = i == 0 && state.isPaid,
                isInstallment = state.isDivideValue,
                type = TransactionType.REPEAT
            )
        }
    }
}

private fun TransactionUIState.toDomain(amount: Double, dateForDb: String, id: String? = null) = Transaction(
    id = id ?: java.util.UUID.randomUUID().toString(),
    title = this.title,
    amount = amount,
    date = dateForDb,
    category = this.category!!,
    direction = this.direction,
    creditCardId = this.creditCardId,
    isPaid = this.isPaid,
    type = this.type,
    installmentCount = if(this.type == TransactionType.REPEAT) this.installmentCount else 0,
    currentInstallment = this.currentInstallment,
    groupId = this.groupId,
    isInstallment = this.isInstallment
)
