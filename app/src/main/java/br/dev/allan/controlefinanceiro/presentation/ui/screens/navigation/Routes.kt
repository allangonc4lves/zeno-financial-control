package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object TransactionsRoute

@Serializable
object ReportsRoute

// Rota de detalhes (Exemplo de rota com parâmetro)
@Serializable
data class TransactionDetailRoute(
    val id: Long
)
