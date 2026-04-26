package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.ExpensesByCategoryCard
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoDrawBoxTop
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.components.DateHeader
import br.dev.allan.controlefinanceiro.presentation.ui.components.TransactionItemRow
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.TotalExpAndIncByMonthCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import br.dev.allan.controlefinanceiro.presentation.viewmodel.HomeViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.features.detail_transaction.EditTransactionDialog
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MonthTransactionsViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    navViewModel: NavigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMonth = viewModel.selectedMonth
    val transactionsViewModel: MonthTransactionsViewModel = hiltViewModel()

    var selectedTransaction by remember { mutableStateOf<TransactionUIState?>(null) }

    selectedTransaction?.let { transaction ->
        val totalIncome = uiState.transactions
            .filter { it.direction == br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection.INCOME }
            .sumOf { it.amount }

        val totalPaidExpenses = uiState.transactions
            .filter { it.direction == br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection.EXPENSE && it.isPaid }
            .sumOf { it.amount }

        EditTransactionDialog(
            transaction = transaction,
            totalIncome = totalIncome,
            totalPaidExpenses = totalPaidExpenses,
            onDismiss = { selectedTransaction = null },
            onConfirm = { updated, editAll ->
                transactionsViewModel.updateTransaction(updated, editAll)
                selectedTransaction = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            ZenoDrawBoxTop {
                TotalExpAndIncByMonthCard(
                    uiState.incomes,
                    uiState.expenses,
                    uiState.balance,
                    selectedMonth,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
            CustomTextTitle(
                text = stringResource(R.string.expenses_by_category),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                startPadding = 8,
            )
            Spacer(modifier = Modifier.size(8.dp))
            if (uiState.chartDataValues.isNotEmpty()) {
                ExpensesByCategoryCard(
                    chartDataValues = uiState.chartDataValues,
                    chartDataLabels = uiState.chartDataLabels
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zeno_not_found),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Inside
                    )
                    CustomTextContent(
                        text = stringResource(R.string.no_expenses_found),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CustomTextTitle(
                    text = stringResource(R.string.recent_activities),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    startPadding = 8
                )
                CustomTextContent(
                    text = stringResource(R.string.view_all),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clickable {
                        navViewModel.navigateWithOptions(navController, TransactionsRoute)
                    },
                    startPadding = 0,
                    endPadding = 8,
                )
            }
        }

        if (uiState.transactions.isNotEmpty()) {
            val groupedTransactions = uiState.transactions.groupBy { it.formattedDate }

            groupedTransactions.forEach { (date, transactions) ->
                item {
                    DateHeader(dateMillis = transactions.first().dateMillis)
                }

                items(
                    items = transactions,
                    key = { it.id }
                ) { item ->
                    TransactionItemRow(
                        uiModel = item,
                        onClick = {
                            selectedTransaction = item
                        }
                    )
                }
            }
        } else {
            item {
                Spacer(modifier = Modifier.size(64.dp))
                CustomTextContent(
                    text = stringResource(R.string.no_records_last_10_days),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
