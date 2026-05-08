package br.dev.allan.controlefinanceiro.domain.model

import br.dev.allan.controlefinanceiro.constants.TransactionCategory

data class CategorySum(
    val category: TransactionCategory,
    val total: Double
)