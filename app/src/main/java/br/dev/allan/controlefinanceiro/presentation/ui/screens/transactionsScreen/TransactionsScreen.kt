package br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import br.dev.allan.controlefinanceiro.utils.TransactionUIModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MonthTransactionsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    navController: NavHostController,
    viewModel: MonthTransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactionsUiModel.collectAsStateWithLifecycle()
    val currentMonthMillis by viewModel.currentMonth.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { uiModel ->
            val cal = Calendar.getInstance().apply { timeInMillis = uiModel.dateMillis }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSortedMap(compareByDescending { it })
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            MonthSelector(
                currentMonthMillis = currentMonthMillis,
                onPreviousMonth = { viewModel.changeMonth(-1) },
                onNextMonth = { viewModel.changeMonth(1) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zeno_not_found),
                        contentDescription = "Nenhum registro",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Inside
                    )
                    CustomTextContent(
                        text = "Nenhum registro encontrado!",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedTransactions.forEach { (dateMillis, items) ->
                        item {
                            DateHeader(dateMillis)
                        }
                        items(items, key = { it.id }) { uiModel ->
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
    }
}

@Composable
fun DateHeader(dateMillis: Long) {
    val date = Date(dateMillis)
    val dayOfWeek = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(date)
        .replaceFirstChar { it.uppercase() }
    val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(date)

    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            text = "$dayOfWeek, $dayOfMonth",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        )
    }
}

@Composable
fun TransactionItemRow(
    uiModel: TransactionUIModel,
    onTogglePayment: () -> Unit
) {
    val appearance = uiModel.category?.getAppearance()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(color = uiModel.color.copy(alpha = 0.8f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = appearance?.icon ?: Icons.Default.Pending,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiModel.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val source = if (uiModel.creditCardId != null) "Cartão" else "Carteira"
            Text(
                text = "${appearance?.displayName ?: "Outros"} | $source",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = uiModel.formattedAmount,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = uiModel.color
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiModel.creditCardId != null) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF008080),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onTogglePayment,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = if (uiModel.isPaid) Icons.Default.CheckCircle else Icons.Default.Pending,
                        contentDescription = "Status",
                        tint = if (uiModel.isPaid) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardDoubleArrowLeft, contentDescription = "Mês Anterior")
        }

        CustomTextTitle(text = monthLabel)

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = "Próximo Mês")
        }
    }
}
