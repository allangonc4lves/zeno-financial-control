package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@Composable
fun TotalExpAndIncByMonthCard(
    totalBalance: Double,
    totalIncomes: Double,
    totalExpenses: Double,
    selectedMonth: YearMonth,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val isVisible by viewModel.isBalanceVisible.collectAsState()

    val scope = rememberCoroutineScope()

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    fun formatValue(value: Double): String {
        return if (isVisible) currencyFormatter.format(value) else "R$ •••••"
    }

    CustomCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                MonthSelectorMenu(
                    selectedMonth = selectedMonth,
                    onMonthChange = { newMonth -> viewModel.updateMonth(newMonth) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Saldo Total do Mês", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = if (isVisible) "R$ ${
                        String.format(
                            "%.2f",
                            totalBalance
                        )
                    }" else "R$ •••••",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalBalance < 0) Color(0xFFAB1A1A) else Color(0xFF4CAF50)
                    )
                )
            }

            IconButton(
                onClick = {
                    scope.launch { viewModel.toggleBalanceVisibility(!isVisible) }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Alternar Visibilidade",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem(
                    label = "Receitas",
                    value = formatValue(totalIncomes),
                    icon = Icons.Outlined.ArrowUpward,
                    color = Color(0xFF32A642)
                )

                // Item de Despesa
                SummaryItem(
                    label = "Despesas",
                    value = formatValue(totalExpenses),
                    icon = Icons.Outlined.ArrowDownward,
                    color = Color(0xFFAB1A1A)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = color.copy(alpha = 0.1f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
