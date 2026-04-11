package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoFabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.ReportsRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen.ReportsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.TransactionsScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    BackHandler (enabled = true) {
        navController.popBackStack()
    }

    Scaffold(
        topBar = { ZenoTopBar() },
        bottomBar = {
            ZenoBottomAppBar(
                {
                    navController.navigate(HomeRoute) {
                        popUpTo(navController.graph.startDestinationId) {
                            //saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                {
                    navController.navigate(TransactionsRoute) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                {
                    navController.navigate(ReportsRoute){
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                {}
            )
        },
        floatingActionButton = { ZenoFabBottomBar() },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
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
}