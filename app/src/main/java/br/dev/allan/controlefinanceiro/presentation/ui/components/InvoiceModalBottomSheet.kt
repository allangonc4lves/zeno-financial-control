package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.ui.state.ReportItemUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceModalBottomSheet(
    invoice: ReportItemUiModel.Invoice,
    isAmountVisible: Boolean,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onTransactionClick: (String) -> Unit = {},
    cardColor: Color = MaterialTheme.colorScheme.primary
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = CircleShape
            ) {
                Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = cardColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = cardColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = invoice.cardName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.invoice_from, invoice.monthYear),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (invoice.isPaid) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color(0xFFAB1A1A).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (invoice.isPaid) stringResource(R.string.paid) else stringResource(R.string.pending_payment),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.transactions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                    )
                }
                val groupedInTransactions = invoice.transactions.groupBy { it.formattedDate }
                groupedInTransactions.forEach { (_, transactions) ->
                    item {
                        DateHeader(
                            dateMillis = transactions.first().dateMillis,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                    items(transactions) { tx ->
                        TransactionItemRow(
                            uiModel = tx,
                            isAmountVisible = isAmountVisible,
                            onClick = { onTransactionClick(tx.id) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.total_invoice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAmountVisible) invoice.formattedAmount else "••••",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = if (invoice.isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                        )
                    }
                }
            }
        }
    }
}
