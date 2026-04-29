package br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.components.Loading
import br.dev.allan.controlefinanceiro.presentation.ui.components.ZenoDialog
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.SaveCreditCardUiEvent
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CreditCardsViewModel
import br.dev.allan.controlefinanceiro.utils.DateHelper
import br.dev.allan.controlefinanceiro.utils.constants.CreditCardPreviewType
import br.dev.allan.controlefinanceiro.utils.constants.InputModeCustomTextField
import br.dev.allan.controlefinanceiro.utils.toSystemFormatDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveCreditCardDialog(
    cardId: String? = null,
    onDismiss: () -> Unit,
    viewModel: CreditCardsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) {
        if (cardId != null) {
            viewModel.loadCardToEdit(cardId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SaveCreditCardUiEvent.SaveSuccess -> {
                    onDismiss()
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(id = R.string.delete_card_q)) },
            text = { Text(stringResource(id = R.string.delete_card_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeCard()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    ZenoDialog(
        onDismiss = { onDismiss() },
        onConfirm = { viewModel.saveCard() },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextTitle(if (cardId == null) stringResource(id = R.string.new_card) else stringResource(id = R.string.edit_card))
                        if (cardId != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    CustomOutlinedTextField(
                        value = state.bankName,
                        label = stringResource(id = R.string.bank_label),
                        capitalization = KeyboardCapitalization.Sentences,
                        isError = state.bankNameError != null,
                        errorMessage = state.bankNameError?.let {
                            if (it.startsWith("error_res_")) {
                                stringResource(it.removePrefix("error_res_").toInt())
                            } else it
                        } ?: "",
                        onValueChange = { viewModel.onBankNameChange(it) }
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.6f),
                            value = state.brand,
                            label = stringResource(id = R.string.brand_label),
                            capitalization = KeyboardCapitalization.Sentences,
                            isError = state.brandError != null,
                            errorMessage = state.brandError?.let {
                                if (it.startsWith("error_res_")) {
                                    stringResource(it.removePrefix("error_res_").toInt())
                                } else it
                            } ?: "",
                            onValueChange = { viewModel.onBrandChange(it) }
                        )
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.4f),
                            value = state.lastDigits,
                            label = stringResource(id = R.string.last_digits_label),
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 4,
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.NumberPassword,
                            isError = state.lastDigitsError != null,
                            errorMessage = state.lastDigitsError?.let {
                                if (it.startsWith("error_res_")) {
                                    stringResource(it.removePrefix("error_res_").toInt())
                                } else it
                            } ?: "",
                            onValueChange = {
                                viewModel.onLastDigitsChange(it)
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = state.invoiceClosing.toString(),
                            label = stringResource(id = R.string.invoice_closing_label),
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 2,
                            onValueChange = {
                                val day = it.toIntOrNull() ?: 1
                                if (day in 1..28) {
                                    viewModel.onInvoiceClosingChange(day)
                                }
                            }
                        )

                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = state.dueDate.toString(),
                            label = stringResource(id = R.string.due_date_label),
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 2,
                            onValueChange = {
                                val day = it.toIntOrNull() ?: 1
                                if (day in 1..31) {
                                    viewModel.onDueDateChange(day)
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(id = R.string.select_color))
                    ColorSelector(
                        palette = state.palette,
                        initialSelectedColor = state.backgroundColor,
                        onColorSelected = { viewModel.onColorSelected(it) }
                    )
                    Spacer(Modifier.height(4.dp))
                    CreditCardPreview(
                        bankName = state.bankName,
                        brand = state.brand,
                        lastDigits = state.lastDigits,
                        backgroundColorLong = state.backgroundColor,
                        previewType = CreditCardPreviewType.SMALL
                    )
                }

                if (state.isLoading) {
                    Loading()
                }
            }
        }
    )
}