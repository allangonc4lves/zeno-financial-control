package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance
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
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    navViewModel: NavigationViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalIncomes by viewModel.totalIncomes.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val selectedMonth = viewModel.selectedMonth

    val expensesMap by viewModel.chartData.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            ZenoDrawBoxTop {
                TotalExpAndIncByMonthCard(
                    totalBalance,
                    totalIncomes,
                    totalExpenses,
                    selectedMonth
                )
            }
        }

        item {
            if(totalExpenses > 0 ){
                Spacer(modifier = Modifier.size(16.dp))
                CustomTextTitle("Despesas por categoria", MaterialTheme.colorScheme.onPrimaryContainer, 8)
                ExpensesByCategoryCard(expensesMap)
            }
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),//.clickable(){navViewModel.navigateWithOptions(navController, TransactionsRoute)},
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CustomTextTitle("Últimas atividades", MaterialTheme.colorScheme.onPrimaryContainer, 8)
                CustomTextContent(
                    "Ver tudo",
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    Modifier.clickable { navViewModel.navigateWithOptions(navController, TransactionsRoute) },
                    0, 8,
                )
            }
        }

        items(transactions.takeLast(5)) { item ->

            val appearance = item.category.getAppearance()

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable() {  },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = appearance.icon,
                        contentDescription = appearance.displayName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(32.dp)
                    )
                    Column {
                        CustomTextTitle(
                            item.title,
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CustomTextContent(
                            appearance.displayName,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    CustomTextTitle(
                        if (item.type == TransactionDirection.EXPENSE) "- " + "R$ ${item.amount}" else "+ " + "R$ ${item.amount}",
                        if (item.type == TransactionDirection.EXPENSE) Color(0xFFAB1A1A) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    CustomTextContent(
                        SimpleDateFormat(
                            "dd/MM/yyyy", Locale.getDefault()
                        ).format(
                            Date(item.date)
                        ),
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
           // HorizontalDivider(thickness = 2.dp)
        }
    }
}