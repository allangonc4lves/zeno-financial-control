package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.FabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.NavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = { ZenoTopBar(onProfileClick = { showSheet = true }) },
        bottomBar = {
            ZenoBottomAppBar(navController = navController)

        },
        floatingActionButton = { FabBottomBar(currentRoute = currentRoute, navController = navController) },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        NavHost(navController, innerPadding)
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            ProfileSheetContent(
                viewModel = hiltViewModel(),
                onClose = { showSheet = false }
            )
        }
    }
}