package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.FabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.NavHost
import androidx.navigation.NavDestination.Companion.hasRoute
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ProfileSheetContent
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.LoginRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute
import br.dev.allan.controlefinanceiro.presentation.viewmodel.LoginViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isUserLoggedIn by mainViewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == LoginRoute::class.qualifiedName || currentRoute == null) {
                navController.navigate(HomeRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.logoutEvent.collect {
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBars = currentDestination?.hasRoute<LoginRoute>() == false

    Scaffold(
        /*
        topBar = {
            if (showBars) {
                ZenoTopBar()
            }
        },
        */
        bottomBar = {
            if (showBars) {
                ZenoBottomAppBar(
                    navController = navController,
                    onProfileClick = { showSheet = true }
                )
            }
        },
        floatingActionButton = {
            if (showBars) {
                FabBottomBar(currentRoute = currentRoute, navController = navController)
            }
        },
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
                loginViewModel = loginViewModel,
                onClose = { showSheet = false },
                onLogout = {
                    showSheet = false
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
