package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object TransactionsRoute

@Serializable
object ReportsRoute

@Serializable
data class TransactionDetailRoute(
    val id: Long
)

@Serializable
object CreditCardsRoute
