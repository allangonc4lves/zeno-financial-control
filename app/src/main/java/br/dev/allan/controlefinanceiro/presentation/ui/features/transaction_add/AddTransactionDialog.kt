package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components.DropdownAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components.SingleChoiceButtonAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components.SwitchAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { CustomTextTitle("Cadastro de transação", MaterialTheme.colorScheme.primary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.dateMillis)),
                    onValueChange = {},
                    label = { Text("Data") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                SingleChoiceButtonAddTransaction(
                    selectedIncomeOrExpense = state.direction.ordinal,
                    onSelectionChange = { index ->
                        viewModel.onDirectionChange(TransactionDirection.entries[index])
                    }
                )

                SwitchAddTransaction(
                    text = "Transação Fixa",
                    checked = state.transactionType == TransactionType.FIXED,
                    onCheckedChange = { isChecked ->
                        viewModel.onTransactionTypeChange(if (isChecked) TransactionType.FIXED else TransactionType.DEFAULT)
                    },
                    quantityValue = 0,
                    onQuantityChange = {},
                    showQuantity = false
                )

                if (state.direction == TransactionDirection.EXPENSE) {
                    SwitchAddTransaction(
                        text = "Parceladas",
                        checked = state.transactionType == TransactionType.INSTALLMENT,
                        onCheckedChange = { isChecked ->
                            viewModel.onTransactionTypeChange(if (isChecked) TransactionType.INSTALLMENT else TransactionType.DEFAULT)
                        },
                        quantityValue = state.installmentCount,
                        onQuantityChange = { viewModel.onInstallmentCountChange(it) },
                        showQuantity = state.transactionType == TransactionType.INSTALLMENT
                    )
                }

                DropdownAddTransaction(
                    selectedType = state.direction,
                    selectedCategory = state.category,
                    onCategorySelected = { viewModel.onCategoryChange(it) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveTransaction()
                onDismiss()
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}