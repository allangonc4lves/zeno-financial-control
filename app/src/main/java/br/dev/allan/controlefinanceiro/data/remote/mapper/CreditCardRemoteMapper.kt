package br.dev.allan.controlefinanceiro.data.remote.mapper

import br.dev.allan.controlefinanceiro.data.remote.model.CreditCardDto
import br.dev.allan.controlefinanceiro.domain.model.CreditCard

fun CreditCard.toDto(userId: String): CreditCardDto {
    return CreditCardDto(
        id = this.id,
        bankName = this.bankName,
        brand = this.brand,
        lastDigits = this.lastDigits,
        invoiceClosing = this.invoiceClosing,
        dueDate = this.dueDate,
        backgroundColor = this.backgroundColor,
        activated = this.activated,
        userId = userId
    )
}

fun CreditCardDto.toDomain(): CreditCard {
    return CreditCard(
        id = this.id,
        bankName = this.bankName,
        brand = this.brand,
        lastDigits = this.lastDigits,
        invoiceClosing = this.invoiceClosing,
        dueDate = this.dueDate,
        backgroundColor = this.backgroundColor,
        activated = this.activated
    )
}
