package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.domain.model.TransactionType
import br.dev.allan.controlefinanceiro.presentation.ui.components.CircularLoading
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components.DropdownAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components.SingleChoiceButtonAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components.SwitchAddTransaction
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import com.google.android.material.loadingindicator.LoadingIndicator
import java.text.NumberFormat
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

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SaveTransactionUiEvent.SaveSuccess -> {
                    onDismiss()
                }
            }
        }
    }

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
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.isLoading) 0.5f else 1f) // Esmaece o form no loading
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomOutlinedTextField(
                        value = state.title,
                        label = "Título*",
                        capitalization = KeyboardCapitalization.Sentences,
                        isError = state.titleError != null,
                        errorMessage = state.titleError ?: "",
                        onValueChange = { viewModel.onTitleChange(it) }
                    )

                    CustomOutlinedTextField(
                        value = state.amount,
                        label = "Valor*",
                        forceCursorAtEnd = true,
                        keyboardType = KeyboardType.NumberPassword,
                        capitalization = KeyboardCapitalization.None,
                        isError = state.amountError != null,
                        errorMessage = state.amountError ?: "",
                        onValueChange = { viewModel.onAmountChange(it) }
                    )

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
                        onValueChange = { viewModel.onAmountChange(it) },
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
                        onCategorySelected = { viewModel.onCategoryChange(it) },
                        isError = state.categoryError != null,
                        errorMessage = state.categoryError ?: ""
                    )
                }

                if (state.isLoading) {
                    CircularLoading()
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveTransaction()
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}