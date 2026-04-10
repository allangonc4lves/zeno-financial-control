package br.dev.allan.controlefinanceiro.presentation.ui.components

import android.util.Log
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import br.dev.allan.controlefinanceiro.presentation.ui.model.AddTransactionType
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialogAdd(
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    // Estados básicos
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var checkTransactionType by remember { mutableStateOf(AddTransactionType.DEFAULT) }

    // Configuração do DatePicker
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    // Configuração do Dropdown de Categorias
    var selectedType by remember { mutableStateOf(TransactionINorEX.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }

    // Configuração múmero de parcelas
    var installmentCount by remember { mutableIntStateOf(2) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { CustomTextTitle("Cadastro de transação", MaterialTheme.colorScheme.primary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título e Valor (campos anteriores...)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // --- SELETOR DE DATA ---
                OutlinedTextField(
                    value = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Data") },
                    readOnly = true, // Evita abrir o teclado
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextTitle("Tipo de transação", MaterialTheme.colorScheme.primary)

                CustomSingleChoiceSegmentedButton(
                    selectedIncomeOrExpense = selectedType.ordinal,
                    onSelectionChange = { index ->
                        // 1. Atualiza o tipo (Entrada ou Saída) baseado no índice (0 ou 1)
                        selectedType = TransactionINorEX.entries[index]

                        // 2. Reseta a categoria para null
                        // Isso é importante para que o Dropdown volte a exibir "Selecione a Categoria"
                        // e obrigue o usuário a escolher uma categoria válida para o novo tipo selecionado.
                        selectedCategory = null
                    }
                )

                //Verifica se está marcado como fixo
                CustomSwitch(
                    text = "Transação Fixa",
                    quantityValue = 0,
                    onQuantityChange = { newCount ->
                        installmentCount = newCount
                    },
                    showQuantity = false,
                    checked = checkTransactionType == AddTransactionType.FIXED,
                    onCheckedChange = { isChecked ->
                        // Se marcar este, automaticamente o outro desmarca porque o estado muda
                        checkTransactionType =
                            if (isChecked) AddTransactionType.FIXED else AddTransactionType.DEFAULT
                    }
                )

                if(selectedType.ordinal == 1){
                    CustomSwitch(
                        text = "Parceladas",
                        quantityValue = installmentCount,
                        onQuantityChange = { newCount ->
                            installmentCount = newCount
                        },
                        showQuantity = if (checkTransactionType == AddTransactionType.INSTALLMENT) true else false,
                        checked = checkTransactionType == AddTransactionType.INSTALLMENT,
                        onCheckedChange = { isChecked ->
                            checkTransactionType =
                                if (isChecked) AddTransactionType.INSTALLMENT else AddTransactionType.DEFAULT
                        }
                    )
                }

                // --- DROPDOWN DE CATEGORIA ---
                CustomDropdown(
                    selectedType = selectedType,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.i("teste", "installmentCount final: $installmentCount")
                    val newTransaction = Transaction(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                        category = selectedCategory ?: TransactionCategory.OTHERS_EXPENSE,
                        isFixed = checkTransactionType == AddTransactionType.FIXED,
                        isInstallment = checkTransactionType == AddTransactionType.INSTALLMENT,
                        installmentCount = if (checkTransactionType == AddTransactionType.INSTALLMENT) {
                            if (installmentCount in 2..360) installmentCount else 2
                        } else {
                            0
                        },
                        type = selectedType
                    )

                    viewModel.addTransaction(newTransaction)
                    onConfirm(newTransaction)
                    Log.i("teste", "installmentCount final: $installmentCount")
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
