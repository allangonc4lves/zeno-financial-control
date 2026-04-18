package br.dev.allan.controlefinanceiro.presentation.ui.features.detail_transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel

@Composable
fun EditTransactionDialog(
    transaction: TransactionUIModel,
    onDismiss: () -> Unit,
    onConfirm: (TransactionUIModel) -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var isPaid by remember { mutableStateOf(transaction.isPaid) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Editar Transação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { isPaid = !isPaid }
                ) {
                    Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                    Text("Marcar como pago")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedTransaction = transaction.copy(
                    title = title,
                    amount = amount.toDoubleOrNull() ?: transaction.amount,
                    isPaid = isPaid
                )
                onConfirm(updatedTransaction)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}