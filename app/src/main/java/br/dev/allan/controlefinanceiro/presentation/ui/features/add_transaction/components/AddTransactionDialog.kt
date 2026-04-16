package br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.InputModeCustomTextField
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.presentation.ui.components.Loading
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.ZenoDialog
import br.dev.allan.controlefinanceiro.presentation.viewmodel.AddTransactionViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {

    val state = viewModel.uiState
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SaveTransactionUiEvent.SaveSuccess -> {
                    onDismiss()
                }
            }
        }
    }

    ZenoDialog(
        title ="Nova transação",
        onDismiss = { onDismiss() },
        onConfirm = { viewModel.saveTransaction() },
        content = {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.isLoading) 0.5f else 1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = state.title,
                            label = "Título*",
                            capitalization = KeyboardCapitalization.Sentences,
                            isError = state.titleError != null,
                            errorMessage = state.titleError ?: "",
                            onValueChange = { viewModel.onTitleChange(it) }
                        )

                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = state.amount,
                            label = "Valor*",
                            forceCursorAtEnd = true,
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 9,
                            keyboardType = KeyboardType.NumberPassword,
                            capitalization = KeyboardCapitalization.None,
                            isError = state.amountError != null,
                            errorMessage = state.amountError ?: "",
                            onValueChange = { viewModel.onAmountChange(it) }
                        )
                    }

                    CustomOutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                            Date(
                                state.dateMillis
                            )
                        ),
                        label = "Data*",
                        isReadOnly = true,
                        isError = false,
                        errorMessage = "",
                        onValueChange = {},
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Selecionar data"
                                )
                            }
                        }
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
                    if (state.transactionType != TransactionType.INSTALLMENT) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = state.isPaid,
                                onCheckedChange = { viewModel.onPaidChange(it) }
                            )
                            Text("Já está pago?")
                        }
                    }

                    DropdownAddTransaction(
                        selectedType = state.direction,
                        selectedCategory = state.category,
                        onCategorySelected = { viewModel.onCategoryChange(it) },
                        isError = state.categoryError != null,
                        errorMessage = state.categoryError ?: ""
                    )

                    if (viewModel.uiState.category == TransactionCategory.CREDIT_CARD_PAYMENT) {
                        CardSelector(
                            cards = viewModel.uiState.cards,
                            selectedCardId = viewModel.uiState.selectedCardId,
                            onCardSelected = { viewModel.onSelectCard(it) }
                        )
                    }
                }

                if (state.isLoading) {
                    Loading()
                }
            }
        }
    )

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

}