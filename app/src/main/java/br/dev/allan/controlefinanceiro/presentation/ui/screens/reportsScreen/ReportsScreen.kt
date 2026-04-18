package br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
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
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f).padding(16.dp),
                title = { Text("Selecione o período", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Relatório", style = MaterialTheme.typography.titleLarge)

            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(
                    text = "${dateFormat.format(Date(filterState.startDate))} - ${dateFormat.format(Date(filterState.endDate))}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(title = "Receitas", value = uiState.formattedTotalIncome, color = Color(0xFF1B5E20), modifier = Modifier.weight(1f))
            SummaryCard(title = "Despesas", value = uiState.formattedTotalExpense, color = Color(0xFFAB1A1A), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            SummaryCard(title = "Saldo do Período", value = uiState.formattedBalance, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
        }

        LazyRow (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.INVOICES_ONLY,
                    onClick = {
                        val newFilter = if (filterState.typeFilter == TransactionTypeFilter.INVOICES_ONLY)
                            TransactionTypeFilter.ALL else TransactionTypeFilter.INVOICES_ONLY
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text("Cartão de credito") },
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
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
                    label = { Text(filterState.categoryFilter ?: "Categorias") },
                    leadingIcon = {
                        if (selectedCategory != null) {
                            val appearance = TransactionCategory.entries.find { it.name == selectedCategory }?.getAppearance()
                            appearance?.let { Icon(it.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    },
                    trailingIcon = {
                        if (selectedCategory != null) {
                            IconButton(onClick = { viewModel.updateCategoryFilter(null) }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                            }
                        }
                    }
                )
            }

            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.INCOME,
                    onClick = {
                        val newFilter = if (filterState.typeFilter == TransactionTypeFilter.INCOME) TransactionTypeFilter.ALL else TransactionTypeFilter.INCOME
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text("Receitas") }
                )
            }

            item {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.EXPENSE,
                    onClick = {
                        val newFilter = if (filterState.typeFilter == TransactionTypeFilter.EXPENSE) TransactionTypeFilter.ALL else TransactionTypeFilter.EXPENSE
                        viewModel.updateTypeFilter(newFilter)
                    },
                    label = { Text("Despesas") }
                )
            }

            item {
                FilterChip(
                    selected = filterState.statusFilter == TransactionStatusFilter.PAID,
                    onClick = {
                        val newFilter = if (filterState.statusFilter == TransactionStatusFilter.PAID) TransactionStatusFilter.ALL else TransactionStatusFilter.PAID
                        viewModel.updateStatusFilter(newFilter)
                    },
                    label = { Text("Pagos") }
                )
            }

            item {
                FilterChip(
                    selected = filterState.statusFilter == TransactionStatusFilter.UNPAID,
                    onClick = {
                        val newFilter = if (filterState.statusFilter == TransactionStatusFilter.UNPAID) TransactionStatusFilter.ALL else TransactionStatusFilter.UNPAID
                        viewModel.updateStatusFilter(newFilter)
                    },
                    label = { Text("Pendentes") }
                )
            }
        }



        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Itens do Período (${uiState.items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(uiState.items) { item ->
                when (item) {
                    is ReportItem.Transaction -> {
                        Text(text = "${item.model.title} - ${item.model.formattedAmount}")
                    }
                    is ReportItem.Invoice -> {
                        InvoiceItem(
                            invoice = item,
                            onClick = { selectedInvoice = item }
                        )
                    }
                }

                if (selectedInvoice != null) {
                    ModalBottomSheet(
                        onDismissRequest = { selectedInvoice = null },
                        sheetState = sheetState
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text(
                                text = "Detalhes: ${selectedInvoice!!.cardName}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(text = "Fatura de ${selectedInvoice!!.monthYear}", color = Color.Gray)

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            selectedInvoice!!.transactions.forEach { tx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(tx.title)
                                    Text(tx.formattedAmount, fontWeight = FontWeight.Bold)
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
                                Text("Filtrar por Categoria", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                            }

                            item {
                                ListItem(
                                    headlineContent = { Text("Todas as Categorias") },
                                    modifier = Modifier.clickable {
                                        viewModel.updateCategoryFilter(null)
                                        showCategorySheet = false
                                    }
                                )
                            }

                            items(TransactionCategory.entries) { category ->
                                val appearance = category.getAppearance()
                                ListItem(
                                    headlineContent = { Text(appearance.displayName) },
                                    leadingContent = {
                                        Icon(appearance.icon, contentDescription = null, tint = appearance.color)
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
                Divider(modifier = Modifier.padding(vertical = 8.dp))
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
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun InvoiceItem(
    invoice: ReportItem.Invoice,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.cardName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fatura: ${invoice.monthYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (invoice.isPaid) {
                    Text(
                        text = "Paga",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = invoice.formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ver detalhes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

