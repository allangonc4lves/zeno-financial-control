package br.dev.allan.controlefinanceiro.presentation.ui.components

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
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.utils.constants.InputModeCustomTextField
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.font.FontWeight
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.SaveTransactionUiEvent
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.TransactionAction
import br.dev.allan.controlefinanceiro.utils.toSystemFormatDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components.CardSelector
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components.DropdownAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components.SingleChoiceButtonAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components.SwitchAddTransaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveTransactionDialog(
    transactionId: String? = null,
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val onAction = viewModel::onAction
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editAllInstallments by remember { mutableStateOf(false) }

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
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
            title = { Text(stringResource(R.string.delete_transaction_q)) },
            text = { Text(stringResource(R.string.delete_transaction_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(TransactionAction.Delete)
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    ZenoDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onAction(TransactionAction.Save(editAllInstallments)) },
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
                        CustomTextTitle(if (transactionId == null) stringResource(R.string.new_transaction) else stringResource(R.string.edit_transaction))
                        if (transactionId != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = uiState.title,
                            label = stringResource(R.string.title_label),
                            capitalization = KeyboardCapitalization.Sentences,
                            isError = uiState.titleError != null,
                            errorMessage = uiState.titleError?.let { 
                                if (it.startsWith("error_res_")) {
                                    stringResource(it.removePrefix("error_res_").toInt())
                                } else it
                            } ?: "",
                            onValueChange = { onAction(TransactionAction.TitleChanged(it)) },
                        )

                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = uiState.amountInput,
                            label = stringResource(R.string.value_label),
                            forceCursorAtEnd = true,
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 9,
                            keyboardType = KeyboardType.NumberPassword,
                            capitalization = KeyboardCapitalization.None,
                            isError = uiState.amountError != null,
                            errorMessage = uiState.amountError?.let {
                                if (it.startsWith("error_res_")) {
                                    stringResource(it.removePrefix("error_res_").toInt())
                                } else it
                            } ?: "",
                            onValueChange = { onAction(TransactionAction.AmountChanged(it)) }
                        )
                    }

                    CustomOutlinedTextField(
                        value = dateFormat.format(Date(uiState.dateMillis)).toSystemFormatDate(),
                        label = stringResource(R.string.date_label),
                        isReadOnly = true,
                        isError = false,
                        errorMessage = "",
                        onValueChange = {},
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, stringResource(R.string.date_label))
                            }
                        }
                    )

                    if (transactionId == null) {
                        SingleChoiceButtonAddTransaction(
                            selectedIncomeOrExpense = uiState.direction.ordinal,
                            onSelectionChange = { index ->
                                onAction(
                                    TransactionAction.DirectionChanged(
                                        TransactionDirection.entries[index]
                                    )
                                )
                            }
                        )

                        SwitchAddTransaction(
                            text = stringResource(R.string.repeat),
                            checked = uiState.type == TransactionType.REPEAT,
                            onCheckedChange = { isChecked ->
                                onAction(
                                    TransactionAction.TypeChanged(
                                        if (isChecked) TransactionType.REPEAT else TransactionType.DEFAULT
                                    )
                                )
                            },
                            quantityValue = uiState.installmentCount,
                            onQuantityChange = {
                                onAction(
                                    TransactionAction.InstallmentCountChanged(
                                        it
                                    )
                                )
                            },
                            showQuantity = uiState.type == TransactionType.REPEAT
                        )

                        if (uiState.type == TransactionType.REPEAT) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CustomTextContent(
                                    text = stringResource(R.string.divide_total_value),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Checkbox(
                                    checked = uiState.isDivideValue,
                                    onCheckedChange = { onAction(TransactionAction.DivideValueToggle(it)) }
                                )
                            }
                        }
                    }

                    if (uiState.direction == TransactionDirection.EXPENSE) {
                        SwitchAddTransaction(
                            text = stringResource(R.string.credit_card_label),
                            checked = uiState.isCreditCard,
                            onCheckedChange = { onAction(TransactionAction.CreditCardToggle(it)) },
                            quantityValue = 0,
                            onQuantityChange = {},
                            showQuantity = false
                        )

                        if (uiState.isCreditCard) {
                            CardSelector(
                                cards = uiState.cards,
                                selectedCardId = uiState.creditCardId,
                                onCardSelected = { onAction(TransactionAction.CardSelected(it)) }
                            )
                        }
                    }

                    DropdownAddTransaction(
                        selectedType = uiState.direction,
                        selectedCategory = uiState.category,
                        onCategorySelected = { onAction(TransactionAction.CategoryChanged(it)) },
                        isError = uiState.categoryError != null,
                        errorMessage = uiState.categoryError?.let {
                            if (it.startsWith("error_res_")) {
                                stringResource(it.removePrefix("error_res_").toInt())
                            } else it
                        } ?: ""
                    )

                    if (transactionId != null && (uiState.groupId != null || uiState.type == TransactionType.REPEAT)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.edit_options),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editAllInstallments = false }
                        ) {
                            RadioButton(
                                selected = !editAllInstallments,
                                onClick = { editAllInstallments = false }
                            )
                            Text(
                                text = if (uiState.groupId != null) stringResource(R.string.only_this_installment) else stringResource(R.string.only_this_month),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editAllInstallments = true }
                        ) {
                            RadioButton(
                                selected = editAllInstallments,
                                onClick = { editAllInstallments = true }
                            )
                            Text(
                                text = if (uiState.groupId != null) stringResource(R.string.all_installments) else stringResource(R.string.all_months),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (uiState.category == TransactionCategory.OTHERS_EXPENSE) {
                        CardSelector(
                            cards = uiState.cards,
                            selectedCardId = uiState.creditCardId,
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis,
            initialDisplayedMonthMillis = if (transactionId != null) uiState.dateMillis else null,
            selectableDates = remember(uiState.dateMillis, transactionId) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        if (transactionId == null) return true
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = uiState.dateMillis
                        val targetMonth = calendar.get(Calendar.MONTH)
                        val targetYear = calendar.get(Calendar.YEAR)

                        calendar.timeInMillis = utcTimeMillis
                        return calendar.get(Calendar.MONTH) == targetMonth &&
                                calendar.get(Calendar.YEAR) == targetYear
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        if (transactionId == null) return true
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = uiState.dateMillis
                        return year == calendar.get(Calendar.YEAR)
                    }
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date(millis))

                        onAction(TransactionAction.DateChanged(formatted))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
