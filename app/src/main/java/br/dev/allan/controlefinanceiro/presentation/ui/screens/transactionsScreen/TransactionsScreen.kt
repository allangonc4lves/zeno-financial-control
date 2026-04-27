package br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import br.dev.allan.controlefinanceiro.presentation.ui.components.DateHeader
import br.dev.allan.controlefinanceiro.presentation.ui.components.TransactionItemRow
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.features.detail_transaction.EditTransactionDialog
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MonthTransactionsViewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    navController: NavHostController,
    viewModel: MonthTransactionsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val transactions by viewModel.transactionsUiModel.collectAsStateWithLifecycle()
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val currentMonthMillis by viewModel.currentMonth.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredTransactions = remember(transactions, searchQuery) {
        if (searchQuery.isBlank()) transactions
        else {
            val query = searchQuery.trim().lowercase()
            transactions.filter { tx ->
                tx.title.lowercase().contains(query) ||
                tx.formattedAmount.lowercase().contains(query) ||
                tx.category?.name?.lowercase()?.contains(query) == true ||
                tx.dateDisplay.contains(query) ||
                tx.formattedParcelInfo?.lowercase()?.contains(query) == true
            }
        }
    }

    var selectedTransaction by remember { mutableStateOf<TransactionUIState?>(null) }

    selectedTransaction?.let { transaction ->
        val totalIncome = filteredTransactions
            .filter { it.direction == br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection.INCOME }
            .sumOf { it.amount }
        
        val totalPaidExpenses = filteredTransactions
            .filter { it.direction == br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection.EXPENSE && it.isPaid }
            .sumOf { it.amount }

        EditTransactionDialog(
            transaction = transaction,
            totalIncome = totalIncome,
            totalPaidExpenses = totalPaidExpenses,
            onDismiss = { selectedTransaction = null },
            onConfirm = { updated, editAll ->
                viewModel.updateTransaction(updated, editAll)
                selectedTransaction = null
            }
        )
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { uiModel ->
            val cal = Calendar.getInstance().apply { timeInMillis = uiModel.dateMillis }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSortedMap(compareByDescending { it })
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            MonthSelector(
                currentMonthMillis = currentMonthMillis,
                onPreviousMonth = { viewModel.changeMonth(-1) },
                onNextMonth = { viewModel.changeMonth(1) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTransactions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zeno_not_found),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Inside
                    )
                    CustomTextContent(
                        text = stringResource(R.string.no_records_found),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    groupedTransactions.forEach { (dateMillis, items) ->
                        item {
                            DateHeader(dateMillis = dateMillis)
                        }
                        items(items, key = { it.id }) { uiModel ->
                            TransactionItemRow(
                                uiModel = uiModel,
                                onTogglePayment = {
                                    viewModel.togglePayment(uiModel)
                                },
                                onClick = {
                                    selectedTransaction = uiModel
                                }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp) // Ajuste para não sobrepor a bottom bar da MainScreen
        )
    }
}

@Composable
fun MonthSelector(
    currentMonthMillis: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        .format(Date(currentMonthMillis))
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardDoubleArrowLeft, contentDescription = stringResource(R.string.previous_month))
        }

        CustomTextTitle(text = monthLabel)

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = stringResource(R.string.next_month))
        }
    }
}
