package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {
    fun navigateWithOptions(navController: NavHostController, route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun getRouteFromDestination(destination: androidx.navigation.NavDestination?): Any? {
        return when {
            destination?.route?.contains("HomeRoute") == true -> br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute
            destination?.route?.contains("ReportsRoute") == true -> br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.ReportsRoute
            destination?.route?.contains("CreditCardsRoute") == true -> br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.CreditCardsRoute
            else -> null
        }
    }

    fun navigateToForm(navController: NavHostController, route: Any) {
        navController.navigate(route)
    }
}