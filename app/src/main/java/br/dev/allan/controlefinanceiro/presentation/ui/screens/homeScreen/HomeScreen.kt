package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.ExpensesByCategoryCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.TotalExpAndIncByMonthCard
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoDrawBoxTop
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import br.dev.allan.controlefinanceiro.presentation.ui.components.DateHeader
import br.dev.allan.controlefinanceiro.presentation.ui.components.TransactionItemRow
import br.dev.allan.controlefinanceiro.presentation.ui.components.InvoiceModelBottomSheet
import br.dev.allan.controlefinanceiro.presentation.ui.components.InvoiceItem
import br.dev.allan.controlefinanceiro.presentation.ui.components.SaveTransactionDialog
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportItemUiModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMonth = viewModel.selectedMonth
    val isBalanceVisible = uiState.isBalanceVisible

    var selectedInvoice by remember { mutableStateOf<ReportItemUiModel.Invoice?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedTransactionId by remember { mutableStateOf<String?>(null) }

    selectedTransactionId?.let { id ->
        SaveTransactionDialog(
            transactionId = id,
            onDismiss = { selectedTransactionId = null }
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
            Spacer(modifier = Modifier.size(24.dp))
            CustomTextTitle(
                text = stringResource(R.string.transactions),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                startPadding = 8,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        if (uiState.items.isNotEmpty()) {
            uiState.items.forEachIndexed { index, item ->
                when (item) {
                    is ReportItemUiModel.Transaction -> {
                        val previousItem = if (index > 0) uiState.items[index - 1] else null
                        val showHeader = previousItem == null ||
                                (previousItem is ReportItemUiModel.Transaction && previousItem.model.formattedDate != item.model.formattedDate) ||
                                (previousItem is ReportItemUiModel.Invoice)
                        if (showHeader) {
                            item {
                                DateHeader(
                                    dateMillis = item.model.dateMillis,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        item {
                            TransactionItemRow(
                                uiModel = item.model,
                                isAmountVisible = isBalanceVisible,
                                onClick = { selectedTransactionId = item.model.id }
                            )
                        }
                    }
                    is ReportItemUiModel.Invoice -> {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                InvoiceItem(
                                    invoice = item,
                                    isAmountVisible = isBalanceVisible,
                                    onClick = { selectedInvoice = item }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                CustomTextContent(
                    text = stringResource(R.string.no_records_found),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        }
    }

    selectedInvoice?.let { invoice ->
        InvoiceModelBottomSheet(
            invoice = invoice,
            isAmountVisible = isBalanceVisible,
            onDismissRequest = { selectedInvoice = null },
            sheetState = sheetState,
            onTransactionClick = { selectedTransactionId = it }
        )
    }
}
