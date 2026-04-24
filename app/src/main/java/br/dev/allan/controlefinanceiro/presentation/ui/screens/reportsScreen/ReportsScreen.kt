package br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.HorizontalDivider
import br.dev.allan.controlefinanceiro.presentation.ui.components.DateHeader
import br.dev.allan.controlefinanceiro.presentation.ui.components.TransactionItemRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.viewmodel.ReportItem
import br.dev.allan.controlefinanceiro.presentation.viewmodel.ReportViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionStatusFilter
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionTypeFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavHostController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.reportUiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsStateWithLifecycle()
    var selectedInvoice by remember { mutableStateOf<ReportItem.Invoice?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.updateDateRange(start, end)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                title = { Text(stringResource(R.string.select_period), modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.report), style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(
                    text = "${dateFormat.format(Date(filterState.startDate))} - ${
                        dateFormat.format(
                            Date(filterState.endDate)
                        )
                    }",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = stringResource(R.string.incomes),
                value = if (isBalanceVisible) uiState.formattedTotalIncome else "••••",
                color = Color(0xFF1B5E20),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = stringResource(R.string.expenses),
                value = if (isBalanceVisible) uiState.formattedTotalExpense else "••••",
                color = Color(0xFFAB1A1A),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SummaryCard(
                title = stringResource(R.string.period_balance),
                value = if (isBalanceVisible) uiState.formattedBalance else "••••",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.WALLET_ONLY,
                    onClick = {
                        val newFilter =
                            if (filterState.typeFilter == TransactionTypeFilter.WALLET_ONLY)
                                TransactionTypeFilter.ALL else TransactionTypeFilter.WALLET_ONLY
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text(stringResource(R.string.wallet)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.INVOICES_ONLY,
                    onClick = {
                        val newFilter =
                            if (filterState.typeFilter == TransactionTypeFilter.INVOICES_ONLY)
                                TransactionTypeFilter.ALL else TransactionTypeFilter.INVOICES_ONLY
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text(stringResource(R.string.credit_card_label)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            item {
                val selectedCategory = filterState.categoryFilter
                FilterChip(
                    selected = filterState.categoryFilter != null,
                    onClick = {
                        if (filterState.categoryFilter != null) {
                            viewModel.updateCategoryFilter(null)
                        } else {
                            showCategorySheet = true
                        }
                    },
                    label = {
                        val categoryName = if (selectedCategory != null) {
                            TransactionCategory.entries.find { it.name == selectedCategory }
                                ?.getAppearance()?.displayNameRes?.let { stringResource(it) } ?: selectedCategory
                        } else {
                            stringResource(R.string.categories)
                        }
                        Text(categoryName)
                    },
                    leadingIcon = {
                        if (selectedCategory != null) {
                            val appearance =
                                TransactionCategory.entries.find { it.name == selectedCategory }
                                    ?.getAppearance()
                            appearance?.let {
                                Icon(
                                    it.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        if (selectedCategory != null) {
                            IconButton(
                                onClick = { viewModel.updateCategoryFilter(null) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                            }
                        }
                    }
                )
            }
            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.INCOME,
                    onClick = {
                        val newFilter =
                            if (filterState.typeFilter == TransactionTypeFilter.INCOME) TransactionTypeFilter.ALL else TransactionTypeFilter.INCOME
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text(stringResource(R.string.incomes)) }
                )
            }
            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.EXPENSE,
                    onClick = {
                        val newFilter =
                            if (filterState.typeFilter == TransactionTypeFilter.EXPENSE) TransactionTypeFilter.ALL else TransactionTypeFilter.EXPENSE
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text(stringResource(R.string.expenses)) }
                )
            }
            item {
                FilterChip(
                    selected = filterState.statusFilter == TransactionStatusFilter.PAID,
                    onClick = {
                        val newFilter =
                            if (filterState.statusFilter == TransactionStatusFilter.PAID) TransactionStatusFilter.ALL else TransactionStatusFilter.PAID
                        viewModel.updateStatusFilter(newFilter)
                    },
                    label = { Text(stringResource(R.string.paid_filter)) }
                )
            }
            item {
                FilterChip(
                    selected = filterState.statusFilter == TransactionStatusFilter.UNPAID,
                    onClick = {
                        val newFilter =
                            if (filterState.statusFilter == TransactionStatusFilter.UNPAID) TransactionStatusFilter.ALL else TransactionStatusFilter.UNPAID
                        viewModel.updateStatusFilter(newFilter)
                    },
                    label = { Text(stringResource(R.string.pending_filter)) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.period_items_count, uiState.items.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            if (uiState.items.isNotEmpty()) {
                uiState.items.forEachIndexed { index, item ->
                    when (item) {
                        is ReportItem.Transaction -> {
                            val previousItem = if (index > 0) uiState.items[index - 1] else null
                            val showHeader = previousItem == null || 
                                (previousItem is ReportItem.Transaction && previousItem.model.formattedDate != item.model.formattedDate) ||
                                (previousItem is ReportItem.Invoice)
                            if (showHeader) {
                                item {
                                    DateHeader(dateMillis = item.model.dateMillis)
                                }
                            }
                            item {
                                TransactionItemRow(
                                    uiModel = item.model,
                                    isAmountVisible = isBalanceVisible,
                                    onClick = { }
                                )
                            }
                        }
                        is ReportItem.Invoice -> {
                            item {
                                InvoiceItem(
                                    invoice = item,
                                    isAmountVisible = isBalanceVisible,
                                    onClick = { selectedInvoice = item }
                                )
                            }
                        }
                    }
                }
            } else {
                item {
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
                }
            }
        }

        selectedInvoice?.let { invoice ->
            ModalBottomSheet(
                onDismissRequest = { selectedInvoice = null },
                sheetState = sheetState,
                dragHandle = {
                    Surface(
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    ) {
                        Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = invoice.cardName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.invoice_from, invoice.monthYear),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (invoice.isPaid) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color(0xFFAB1A1A).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (invoice.isPaid) stringResource(R.string.paid) else stringResource(R.string.pending_payment),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.transactions),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                            )
                        }
                        val groupedInTransactions = invoice.transactions.groupBy { it.formattedDate }
                        groupedInTransactions.forEach { (date, transactions) ->
                            item {
                                DateHeader(
                                    dateMillis = transactions.first().dateMillis,
                                    modifier = Modifier.padding(start = 0.dp)
                                )
                            }
                            items(transactions) { tx ->
                                TransactionItemRow(
                                    uiModel = tx,
                                    isAmountVisible = isBalanceVisible,
                                    onClick = { }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.total_invoice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBalanceVisible) invoice.formattedAmount else "••••",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCategorySheet) {
            ModalBottomSheet(onDismissRequest = { showCategorySheet = false }) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            stringResource(R.string.filter_by_category),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.all_categories)) },
                            modifier = Modifier.clickable {
                                viewModel.updateCategoryFilter(null)
                                showCategorySheet = false
                            }
                        )
                    }
                    items(TransactionCategory.entries) { category ->
                        val appearance = category.getAppearance()
                        ListItem(
                            headlineContent = { Text(stringResource(appearance.displayNameRes)) },
                            leadingContent = {
                                Icon(
                                    appearance.icon,
                                    contentDescription = null,
                                    tint = appearance.color
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.updateCategoryFilter(category.name)
                                showCategorySheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun InvoiceItem(
    invoice: ReportItem.Invoice,
    isAmountVisible: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (invoice.isPaid) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (invoice.isPaid) Color(0xFF1B5E20).copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (invoice.isPaid) Color(0xFF1B5E20) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = invoice.cardName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.invoice_label, invoice.monthYear),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isAmountVisible) invoice.formattedAmount else "••••",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (invoice.isPaid) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color(0xFFAB1A1A).copy(alpha = 0.1f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (invoice.isPaid) stringResource(R.string.paid).uppercase() else stringResource(R.string.pending_payment).uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                    )
                }
            }
        }
    }
}
