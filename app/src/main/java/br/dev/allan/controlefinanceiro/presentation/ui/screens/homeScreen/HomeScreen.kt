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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.CategoryChartCard
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.DrawBoxTop
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.FinancialSummaryCard
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToReports: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()

    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalIncomes by viewModel.totalIncomes.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val selectedMonth = viewModel.selectedMonth

    val context = LocalContext.current
    //val settingsManager = remember { SettingsManager(context) }

    val expensesMap by viewModel.chartData.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            DrawBoxTop {
                FinancialSummaryCard(
                    totalBalance,
                    totalIncomes,
                    totalExpenses,
                    selectedMonth
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
        }

        item {
            CategoryChartCard(expensesMap)
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
            CustomTextTitle("Últimas atividades", Color.Black, 16)
        }


        items(transactions) { item ->
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
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    CustomTextTitle(
                        if (item.type == TransactionDirection.EXPENSE) "- " + "R$ ${item.amount}" else "+ " + "R$ ${item.amount}",
                        Color.Black
                    )
                    CustomTextContent(
                        SimpleDateFormat(
                            "dd/MM/yyyy", Locale.getDefault()
                        ).format(
                            Date(item.date)
                        ), MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(thickness = 2.dp)
        }
    }
}