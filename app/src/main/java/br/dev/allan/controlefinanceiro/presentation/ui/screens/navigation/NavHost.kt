package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.components.AddCreditCardDialog
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components.AddTransactionDialog
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardsScreen
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

        dialog<AddTransactionRoute> { backStackEntry ->
            val route: AddTransactionRoute = backStackEntry.toRoute()
            AddTransactionDialog(
                transactionId = route.id,
                onDismiss = { navController.popBackStack() }
            )
        }

        composable<ReportsRoute> {
            ReportsScreen(navController)
        }

        composable< CreditCardsRoute> {
            CreditCardsScreen(navController)
        }

        dialog< AddCreditCardRoute> { backStackEntry ->
            val route: AddCreditCardRoute = backStackEntry.toRoute()
            AddCreditCardDialog(
                cardId = route.id,
                onDismiss = { navController.popBackStack() }
            )
        }

    }
}