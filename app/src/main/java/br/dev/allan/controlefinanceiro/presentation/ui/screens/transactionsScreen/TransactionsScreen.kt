package br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MonthTransactionsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    navController: NavHostController,
    viewModel: MonthTransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactionsUiModel.collectAsStateWithLifecycle()
    val currentMonthMillis by viewModel.currentMonth.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MonthSelector(
            currentMonthMillis = currentMonthMillis,
            onPreviousMonth = { viewModel.changeMonth(-1) },
            onNextMonth = { viewModel.changeMonth(1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zeno_not_found),
                        contentDescription = "Minha imagem",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Inside
                    )
                    CustomTextContent(
                        text = "Nenhuma registro encontrado!",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(transactions, key = { it.id }) { uiModel ->
                    TransactionItemRow(
                        uiModel = uiModel,
                        onTogglePayment = {
                            viewModel.togglePayment(uiModel)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    currentMonthMillis: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        .format(Date(currentMonthMillis))
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 64.dp, end = 64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardDoubleArrowLeft, contentDescription = "Mês Anterior")
        }

        CustomTextTitle( text = monthLabel )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = "Próximo Mês")
        }
    }
}

@Composable
fun TransactionItemRow(
    uiModel: TransactionUIModel,
    onTogglePayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone de Status (Pago ou Pendente)
            IconButton(onClick = onTogglePayment) {
                Icon(
                    imageVector = if (uiModel.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Status de Pagamento",
                    tint = if (uiModel.isPaid) Color(0xFF4CAF50) else Color.Gray
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiModel.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uiModel.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = uiModel.formattedAmount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = uiModel.color // Vermelho para gasto, Verde para ganho
            )
        }
    }
}