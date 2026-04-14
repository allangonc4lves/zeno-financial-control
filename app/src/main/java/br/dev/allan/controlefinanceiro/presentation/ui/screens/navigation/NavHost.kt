package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardsScreenContainer
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen.ReportsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.TransactionsScreen

@Composable
fun NavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<HomeRoute> {
            HomeScreen(navController)
        }

        composable<TransactionsRoute> {
            TransactionsScreen(navController)
        }

        composable<ReportsRoute> {
            ReportsScreen(navController)
        }

        composable< CreditCardsRoute> {
            CreditCardsScreenContainer(
                onCardClick = { cardId ->
                    // exemplo de navegação para detalhes
                    navController.navigate("cardDetails/$cardId")
                }
            )
        }

        composable("cardDetails/{cardId}") { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
            // aqui você pode mostrar a tela de detalhes usando cardId
            // CardDetailsScreen(cardId = cardId)
        }

    }
}