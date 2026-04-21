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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import br.dev.allan.controlefinanceiro.utils.constants.InputModeCustomTextField
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import br.dev.allan.controlefinanceiro.presentation.ui.components.Loading
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.components.ZenoDialog
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.TransactionAction
import br.dev.allan.controlefinanceiro.utils.toSystemFormatDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    transactionId: Int? = null,
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val onAction = viewModel::onAction
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null && transactionId != -1) {
            viewModel.loadToEdit(transactionId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SaveTransactionUiEvent.SaveSuccess -> onDismiss()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Transação?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    onAction(TransactionAction.Delete) // Usando Action
                    showDeleteConfirm = false
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ZenoDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onAction(TransactionAction.Save) }, // Usando Action
        content = {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (uiState.isLoading) 0.5f else 1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextTitle(if (transactionId == null || transactionId == -1) "Nova transação" else "Editar transação")
                        if (transactionId != null && transactionId != -1) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = uiState.title,
                            label = "Título*",
                            capitalization = KeyboardCapitalization.Sentences,
                            isError = uiState.titleError != null,
                            errorMessage = uiState.titleError ?: "",
                            onValueChange = { onAction(TransactionAction.TitleChanged(it)) },
                        )

                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = uiState.amount,
                            label = "Valor*",
                            forceCursorAtEnd = true,
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 9,
                            keyboardType = KeyboardType.NumberPassword,
                            capitalization = KeyboardCapitalization.None,
                            isError = uiState.amountError != null,
                            errorMessage = uiState.amountError ?: "",
                            onValueChange = { onAction(TransactionAction.AmountChanged(it)) }
                        )
                    }

                    CustomOutlinedTextField(
                        value = dateFormat.format(Date(uiState.dateMillis)).toSystemFormatDate(),
                        label = "Data*",
                        isReadOnly = true,
                        isError = false,
                        errorMessage = "",
                        onValueChange = {},
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, "Data")
                            }
                        }
                    )

                    SingleChoiceButtonAddTransaction(
                        selectedIncomeOrExpense = uiState.direction.ordinal,
                        onSelectionChange = { index ->
                            onAction(TransactionAction.DirectionChanged(TransactionDirection.entries[index]))
                        }
                    )

                    SwitchAddTransaction(
                        text = "Repetir",
                        checked = uiState.transactionType == TransactionType.INSTALLMENT,
                        onCheckedChange = { isChecked ->
                            onAction(TransactionAction.TypeChanged(if (isChecked) TransactionType.INSTALLMENT else TransactionType.DEFAULT))
                        },
                        quantityValue = uiState.installmentCount,
                        onQuantityChange = { onAction(TransactionAction.InstallmentCountChanged(it)) },
                        showQuantity = uiState.transactionType == TransactionType.INSTALLMENT
                    )

                    SwitchAddTransaction(
                        text = "Parcelar",
                        checked = uiState.transactionType == TransactionType.INSTALLMENT,
                        onCheckedChange = { isChecked ->
                            onAction(TransactionAction.TypeChanged(if (isChecked) TransactionType.INSTALLMENT else TransactionType.DEFAULT))
                        },
                        quantityValue = uiState.installmentCount,
                        onQuantityChange = { onAction(TransactionAction.InstallmentCountChanged(it)) },
                        showQuantity = uiState.transactionType == TransactionType.INSTALLMENT
                    )

                    DropdownAddTransaction(
                        selectedType = uiState.direction,
                        selectedCategory = uiState.category,
                        onCategorySelected = { onAction(TransactionAction.CategoryChanged(it)) },
                        isError = uiState.categoryError != null,
                        errorMessage = uiState.categoryError ?: ""
                    )

                    if (uiState.category == TransactionCategory.OTHERS_EXPENSE) {
                        CardSelector(
                            cards = uiState.cards,
                            selectedCardId = uiState.selectedCardId,
                            onCardSelected = { onAction(TransactionAction.CardSelected(it)) }
                        )
                    }
                }

                if (uiState.isLoading) {
                    Loading()
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }.format(java.util.Date(millis))

                        onAction(TransactionAction.DateChanged(formatted))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}