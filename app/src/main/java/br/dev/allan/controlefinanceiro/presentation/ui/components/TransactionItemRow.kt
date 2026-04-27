package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.getAppearance
import br.dev.allan.controlefinanceiro.presentation.ui.state.TransactionUIState
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection

import androidx.compose.ui.res.stringResource
import br.dev.allan.controlefinanceiro.R

@Composable
fun TransactionItemRow(
    uiModel: TransactionUIState,
    isAmountVisible: Boolean = true,
    onClick: () -> Unit = {},
    onTogglePayment: (() -> Unit)? = null
) {
    val appearance = uiModel.category?.getAppearance()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiModel.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                uiModel.formattedParcelInfo?.let { info ->
                    Text(
                        text = " ($info)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            val source = if (uiModel.creditCardId != null) stringResource(R.string.card) else stringResource(R.string.wallet)
            val categoryName = appearance?.displayNameRes?.let { stringResource(it) } ?: stringResource(R.string.others)
            Text(
                text = "$categoryName | $source",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (isAmountVisible) uiModel.formattedAmount else "••••",
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

                if (uiModel.direction != TransactionDirection.INCOME) {
                    if (onTogglePayment != null) {
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
                    } else {
                        Icon(
                            imageVector = if (uiModel.isPaid) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = "Status",
                            tint = if (uiModel.isPaid) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
