package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object TransactionsRoute

@Serializable
data class AddTransactionRoute(val id: Int? = null)

@Serializable
object ReportsRoute

@Serializable
object CreditCardsRoute

@Serializable
data class AddCreditCardRoute(val id: String? = null)


