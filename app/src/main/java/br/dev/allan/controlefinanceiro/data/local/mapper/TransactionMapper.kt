package br.dev.allan.controlefinanceiro.data.local.mapper

import android.util.Log
import androidx.compose.ui.graphics.Color
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.utils.CurrencyManager
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        date = date,
        category = category,
        type = type,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        currentInstallment = currentInstallment,
        isPaid = isPaid,
        direction = direction,
        creditCardId = creditCardId,
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(

        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        date = date,
        category = category,
        type = type,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        currentInstallment = currentInstallment,
        isPaid = isPaid,
        direction = direction,
        creditCardId = creditCardId
    )
}

fun Transaction.toUi(currencyManager: CurrencyManager, code: String): TransactionUIModel {
    val prefix = if (direction == TransactionDirection.EXPENSE) "- " else "+ "

    val dateObj = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) ?: Date()
    } catch (e: Exception) {
        Date()
    }

    return TransactionUIModel(
        id = id,
        title = title,
        amount = amount,
        dateMillis = dateObj.time,
        formattedAmount = prefix + currencyManager.formatByCurrencyCode(amount, code),
        formattedDate = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(dateObj),
        color = if (direction == TransactionDirection.EXPENSE) Color(0xFFAB1A1A) else Color(0xFF1B5E20),
        category = category,
        type = type,
        direction = direction,
        isPaid = isPaid,
        isInstallment = isInstallment,
        currentInstallment = currentInstallment,
        installmentCount = installmentCount,
        creditCardId = creditCardId,
        formattedTotalAmount = currencyManager.formatByCurrencyCode(amount, code),
        formattedParcelInfo = null
    )
}

fun TransactionUIModel.toDomain(amount: Double, dateForDb: String, id: Int = 0) = Transaction(
    id = id,
    title = this.title,
    amount = amount,
    date = dateForDb,
    category = this.category!!,
    direction = this.direction,
    creditCardId = this.creditCardId,
    isPaid = this.isPaid,
    type = this.type,
    installmentCount = if(this.type == TransactionType.REPEAT) this.installmentCount else 0
)

fun TransactionEntity.toDomain(
    payments: List<PaymentStatusEntity> = emptyList(),
    monthYear: String? = null
): Transaction {
    val displayDate = this.date

    val isPaidResult = if (monthYear != null) {
        payments.any { it.transactionId == this.id.toString() && it.monthYear == monthYear } || this.isPaid
    } else {
        this.isPaid
    }

    return Transaction(
        id = id,
        title = title,
        amount = amount,
        date = displayDate,
        isPaid = isPaidResult,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        currentInstallment = currentInstallment,
        creditCardId = creditCardId,
        category = category,
        direction = direction,
        type = type,
    )
}