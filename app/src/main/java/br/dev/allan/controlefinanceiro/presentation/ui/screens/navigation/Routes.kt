package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object LoginRoute

@Serializable
data class SaveTransactionRoute(val id: String? = null)

@Serializable
object TransactionsRoute

@Serializable
object CreditCardsRoute

@Serializable
data class SaveCreditCardRoute(val id: String? = null)


