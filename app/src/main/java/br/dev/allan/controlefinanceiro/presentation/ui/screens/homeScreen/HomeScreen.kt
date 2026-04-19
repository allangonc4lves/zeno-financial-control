package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.ExpensesByCategoryCard
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoDrawBoxTop
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.TotalExpAndIncByMonthCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.AddTransactionRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import br.dev.allan.controlefinanceiro.presentation.viewmodel.HomeViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import br.dev.allan.controlefinanceiro.util.toSystemFormatDate

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    navViewModel: NavigationViewModel = hiltViewModel()
) {
    val recentsTransactions by viewModel.recentTransactionsUI.collectAsState()
    val formattedIncomes by viewModel.formattedIncomes.collectAsState()
    val formattedExpenses by viewModel.formattedExpenses.collectAsState()
    val formattedBalance by viewModel.formattedBalance.collectAsState()
    val selectedMonth = viewModel.selectedMonth

    val chartData by viewModel.chartData.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            ZenoDrawBoxTop {
                TotalExpAndIncByMonthCard(
                    formattedIncomes,
                    formattedExpenses,
                    formattedBalance,
                    selectedMonth
                )
            }
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
            CustomTextTitle(
                text = "Despesas por categoria",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                startPadding = 8
            )
            ExpensesByCategoryCard(chartData)
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CustomTextTitle(
                    text = "Atividades recentes",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    startPadding = 8
                )
                CustomTextContent(
                    text = "Ver tudo",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clickable {
                        navViewModel.navigateWithOptions(
                            navController,
                            TransactionsRoute
                        )
                    },
                    startPadding = 0,
                    endPadding = 8,
                )
            }
        }
        if (recentsTransactions.isNotEmpty()) {
            items(recentsTransactions) { item ->

                val appearance = item.category.getAppearance()

                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            navViewModel.navigateToForm(
                                navController = navController,
                                route = AddTransactionRoute(id = item.id)
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (item.direction.name == TransactionDirection.EXPENSE.name) Color(
                                        0xFFAB1A1A
                                    )
                                    else Color(0xFF32A642),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = appearance.icon,
                                contentDescription = appearance.displayName,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            CustomTextTitle(
                                item.title,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CustomTextContent(
                                text = appearance.displayName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        CustomTextTitle(
                            text = item.formattedAmount,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        CustomTextContent(
                            text = item.formattedDate.toSystemFormatDate(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CustomTextContent(
                        text = "Nenhuma transação registrada nos últimos 10 dias",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
