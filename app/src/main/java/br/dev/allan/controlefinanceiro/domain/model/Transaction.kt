package br.dev.allan.controlefinanceiro.domain.model

import br.dev.allan.controlefinanceiro.R

data class Transaction(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: TransactionCategory,
    val isFixed: Boolean = false,
    val isInstallment: Boolean = false,
    val installmentCount: Int = 0,
    val type: TransactionINorEX,
)


