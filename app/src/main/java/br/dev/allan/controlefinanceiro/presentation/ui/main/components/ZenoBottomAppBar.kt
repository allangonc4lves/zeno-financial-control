package br.dev.allan.controlefinanceiro.presentation.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.dev.allan.controlefinanceiro.domain.model.ButtonAppBarNavigation
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.ReportsRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.CreditCardsRoute

@Composable
fun ZenoBottomAppBar(
    navController: NavHostController,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val buttonsAppBarList = listOf(
        ButtonAppBarNavigation("Home", Icons.Rounded.Home, HomeRoute),
        ButtonAppBarNavigation("Transações", Icons.Rounded.CompareArrows, TransactionsRoute),
        ButtonAppBarNavigation("Relatórios", Icons.Rounded.Analytics, ReportsRoute),
        ButtonAppBarNavigation("CreditCards", Icons.Rounded.CreditCard, CreditCardsRoute)
    )

    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.graphicsLayer {
            clip = true
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            buttonsAppBarList.forEachIndexed { index, item ->
                if (index == 2) Spacer(modifier = Modifier.width(48.dp))

                val isSelected = currentDestination?.hasRoute(item.route::class) ?: false

                ButtonBarNavigation(
                    item = item,
                    isSelected = isSelected,
                    onClick = { viewModel.navigateWithOptions(navController, item.route) }
                )
            }
        }
    }
}