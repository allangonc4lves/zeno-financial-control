package br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen.ReportsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.TransactionsScreen

@Composable
fun ZenoNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToTransactions = {
                    navController.navigate(TransactionsRoute)
                },
                onNavigateToReports = {
                    navController.navigate(ReportsRoute)
                }
            )
        }

        composable<TransactionsRoute> {
            TransactionsScreen()
        }

        composable<ReportsRoute> {
            ReportsScreen()
        }
    }
}