package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.HomeViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth

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

    CustomCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                MonthSelectorMenu(
                    selectedMonth = selectedMonth,
                    onMonthChange = { newMonth -> viewModel.updateMonth(newMonth) }
                )
            }

            Column(
                modifier = Modifier
                    .padding(4.dp)
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
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Receitas", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = if (isVisible) "R$ ${
                            String.format(
                                "%.2f",
                                totalIncomes
                            )
                        }" else "R$ •••",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Despesas", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = if (isVisible) "R$ ${
                            String.format(
                                "%.2f",
                                totalExpenses
                            )
                        }" else "R$ •••",
                        color = Color(0xFFAB1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}