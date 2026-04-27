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
import br.dev.allan.controlefinanceiro.presentation.ui.components.SaveTransactionDialog
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.login.LoginScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen.ReportsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.TransactionsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    val auth = FirebaseAuth.getInstance()
    
    // Verifica se o usuário existe no Auth e força reload para checar se a conta ainda é válida
    val currentUser = auth.currentUser
    
    // Lógica de destino inicial baseada apenas no Auth
    val startDest = if (currentUser == null) LoginRoute else HomeRoute

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<HomeRoute> {
            HomeScreen(navController)
        }

        composable<TransactionsRoute> {
            TransactionsScreen(navController)
        }

        dialog<AddTransactionRoute> { backStackEntry ->
            val route: AddTransactionRoute = backStackEntry.toRoute()
            SaveTransactionDialog(
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