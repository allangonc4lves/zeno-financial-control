package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoFabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.ReportsRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.ZenoNavHost
import br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen.ReportsScreen
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.TransactionsScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { ZenoTopBar() },
        bottomBar = {
            ZenoBottomAppBar(navController = navController)

        },
        floatingActionButton = { ZenoFabBottomBar() },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        ZenoNavHost(navController, innerPadding)
    }
}