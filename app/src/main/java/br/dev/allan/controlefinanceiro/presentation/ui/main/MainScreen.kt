package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoFabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.NavHost

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
        NavHost(navController, innerPadding)
    }
}