package br.dev.allan.controlefinanceiro.data.local.mapper

import br.dev.allan.controlefinanceiro.data.local.CreditCardEntity
import br.dev.allan.controlefinanceiro.domain.model.CreditCard

fun CreditCardEntity.toDomain() = CreditCard(
    id = id,
    bankName = bankName,
    brand = brand,
    lastDigits = lastDigits,
    backgroundColor = backgroundColor
)

fun CreditCard.toEntity() = CreditCardEntity(
    id = id,
    bankName = bankName,
    brand = brand,
    lastDigits = lastDigits,
    backgroundColor = backgroundColor
)