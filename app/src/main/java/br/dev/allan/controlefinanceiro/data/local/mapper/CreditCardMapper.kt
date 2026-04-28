package br.dev.allan.controlefinanceiro.data.local.mapper

import br.dev.allan.controlefinanceiro.data.local.CreditCardEntity
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import kotlin.String

fun CreditCardEntity.toDomain() = CreditCard(
    id = id,
    bankName = bankName,
    brand = brand,
    lastDigits = lastDigits,
    invoiceClosing = invoiceClosing,
    dueDate = dueDate,
    backgroundColor = backgroundColor,
    activated = activated
)

fun CreditCard.toEntity() = CreditCardEntity(
    id = id,
    bankName = bankName,
    brand = brand,
    lastDigits = lastDigits,
    invoiceClosing = invoiceClosing,
    dueDate = dueDate,
    backgroundColor = backgroundColor,
    activated = activated
)