package br.dev.allan.controlefinanceiro.presentation.ui.screens.TransactionScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ExpenseScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val transaction by viewModel.transactions.collectAsState()

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Valor") })


        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn() {
            items(transaction) { expense ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(expense.title, style = MaterialTheme.typography.titleMedium)
                        Text("R$ ${expense.amount}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expense.date)),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row {
                            Button(onClick = { viewModel.deleteTransaction(expense) }) {
                                Text("Excluir")
                            }
                        }
                    }
                }
            }
        }
    }
}