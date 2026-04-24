package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomCard
import br.dev.allan.controlefinanceiro.presentation.viewmodel.HomeViewModel
import androidx.compose.ui.res.stringResource
import br.dev.allan.controlefinanceiro.R
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun TotalExpAndIncByMonthCard(
    formattedIncomes: String,
    formattedExpenses: String,
    formattedBalance: String,
    selectedMonth: YearMonth,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    CustomCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthSelectorMenu(
                    selectedMonth = selectedMonth,
                    onMonthChange = { newMonth -> viewModel.updateMonth(newMonth) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MainBalanceItem(
                    label = stringResource(id = R.string.balance),
                    value = if (uiState.isBalanceVisible) formattedBalance else "•••••",
                    rawValue = uiState.rawBalance,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                MainBalanceItem(
                    label = stringResource(id = R.string.available_balance),
                    value = if (uiState.isBalanceVisible) uiState.availableBalance else "•••••",
                    rawValue = uiState.rawAvailableBalance,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryItem(
                        label = stringResource(id = R.string.incomes),
                        value = if (uiState.isBalanceVisible) formattedIncomes else "•••••",
                        icon = Icons.Outlined.ArrowUpward,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            scope.launch { viewModel.toggleBalanceVisibility(!uiState.isBalanceVisible) }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(id = R.string.toggle_visibility),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    SummaryItem(
                        label = stringResource(id = R.string.expenses),
                        value = if (uiState.isBalanceVisible) formattedExpenses else "•••••",
                        icon = Icons.Outlined.ArrowDownward,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryItem(
                        label = stringResource(id = R.string.paid_filter),
                        value = if (uiState.isBalanceVisible) uiState.paidValue else "•••••",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        color = Color(0xFF455A64),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.size(32.dp))

                    SummaryItem(
                        label = stringResource(id = R.string.pending_filter),
                        value = if (uiState.isBalanceVisible) uiState.pendingValue else "•••••",
                        icon = Icons.Outlined.PendingActions,
                        color = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainBalanceItem(
    label: String,
    value: String,
    rawValue: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (rawValue < 0) Color(0xFFC62828) else Color(0xFF2E7D32)
            )
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = color.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
