package br.dev.allan.controlefinanceiro.data.local.mapper

import android.util.Log
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        date = date,
        category = category,
        isFixed = isFixed,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        direction = direction,
        creditCardId = creditCardId
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        date = date,
        category = category,
        isFixed = isFixed,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        direction = direction,
        creditCardId = creditCardId
    )
}

fun TransactionEntity.toDomain(
    payments: List<PaymentStatusEntity>,
    monthYear: String?
): Transaction {

    val isPaidThisMonth = if (!monthYear.isNullOrBlank()) {
        payments.any { payment ->
            payment.transactionId == this.id.toString() &&
                    payment.monthYear == monthYear
        }
    } else {
        this.isPaid
    }

    return Transaction(
        id = id,
        title = title,
        amount = amount,
        date = date,
        isPaid = isPaidThisMonth,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        creditCardId = creditCardId,
        category = category,
        direction = direction
    )
}