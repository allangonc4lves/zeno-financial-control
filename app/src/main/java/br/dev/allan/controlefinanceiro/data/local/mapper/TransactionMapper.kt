package br.dev.allan.controlefinanceiro.data.local.mapper

import android.util.Log
import br.dev.allan.controlefinanceiro.data.local.PaymentStatusEntity
import br.dev.allan.controlefinanceiro.data.local.TransactionEntity
import br.dev.allan.controlefinanceiro.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        groupId = groupId,
        title = title,
        amount = amount,
        date = date,
        category = category,
        type = type,
        isFixed = isFixed,
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
        isFixed = isFixed,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        currentInstallment = currentInstallment,
        isPaid = isPaid,
        direction = direction,
        creditCardId = creditCardId
    )
}
/*
fun TransactionEntity.toDomain(
    payments: List<PaymentStatusEntity>,
    monthYear: String?,
): Transaction {

    val isPaidThisMonth = if (!monthYear.isNullOrBlank()) {
        payments.any { payment ->
            payment.transactionId == this.id.toString()
            && payment.monthYear == monthYear
        }
    } else {
        if (this.creditCardId != null) false else this.isPaid
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
        direction = direction,
        type = type,
        isFixed = isFixed
    )
}*/

fun TransactionEntity.toDomain(
    payments: List<PaymentStatusEntity> = emptyList(),
    monthYear: String? = null,
    viewedMonthMillis: Long = this.date
): Transaction {

    val displayDate = if (this.isFixed) {
        val calendarOriginal = java.util.Calendar.getInstance().apply { timeInMillis = this@toDomain.date }
        val calendarViewed = java.util.Calendar.getInstance().apply { timeInMillis = viewedMonthMillis }

        calendarViewed.set(java.util.Calendar.DAY_OF_MONTH, calendarOriginal.get(java.util.Calendar.DAY_OF_MONTH))
        calendarViewed.timeInMillis
    } else {
        this.date
    }

    val isPaidThisMonth = payments.any {
        it.transactionId == this.id.toString() && it.monthYear == monthYear
    }

    return Transaction(
        id = id,
        title = title,
        amount = amount,
        date = if (this.isFixed) displayDate else this.date,
        isPaid = if (this.isFixed) isPaidThisMonth else this.isPaid,
        isInstallment = isInstallment,
        installmentCount = installmentCount,
        creditCardId = creditCardId,
        category = category,
        direction = direction,
        type = type,
        isFixed = isFixed
    )
}