package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.AddCreditCardRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.AddTransactionRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.MonthSelector
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CreditCardTransactionViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel

@Composable
fun CreditCardsScreen(
    navController: NavHostController,
    viewModel: CreditCardTransactionViewModel = hiltViewModel(),
    navViewModel: NavigationViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val transactions by viewModel.transactionsUiState.collectAsState()
    val totalOpenInvoices by viewModel.totalOpenInvoicesState.collectAsState()
    val chartData by viewModel.chartDataState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedMonthTotal by viewModel.formattedSelectedMonthTotal.collectAsState()

    val pagerState = rememberPagerState(pageCount = { cards.size })

    val selectedCardColor =
        if (cards.isNotEmpty()) Color(cards[pagerState.currentPage].backgroundColor) else Color.Gray

    LaunchedEffect(pagerState.currentPage, cards) {
        if (cards.isNotEmpty()) {
            viewModel.setSelectedCard(cards[pagerState.currentPage].id)
        }
    }

    val cardWidth: Dp = 300.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSize = PageSize.Fixed(cardWidth),
            contentPadding = PaddingValues(horizontal = (LocalConfiguration.current.screenWidthDp.dp - cardWidth) / 2),
            pageSpacing = 16.dp,
        ) { page ->
            val card = cards[page]
            CreditCardPreview(
                bankName = card.bankName,
                brand = card.brand,
                lastDigits = card.lastDigits.toString(),
                backgroundColorLong = card.backgroundColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clickable {
                        navViewModel.navigateToForm(
                            navController = navController,
                            route = AddCreditCardRoute(id = card.id)
                        )
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MonthSelector(
            currentMonthMillis = currentMonth,
            onPreviousMonth = { viewModel.changeMonth(-1) },
            onNextMonth = { viewModel.changeMonth(1) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                if (chartData.isNotEmpty()) {
                    CreditCardBarChart(
                        data = chartData,
                        barColor = selectedCardColor,
                        totalMonth = selectedMonthTotal,
                        totalOpenInvoices = totalOpenInvoices,
                        modifier = Modifier.fillMaxWidth(),
                        onStatusClick = { isPaid ->
                            viewModel.markMonthAsPaid(isPaid)
                        }
                    )
                }
            }
            items(transactions, key = { it.id }) { transaction ->
                CardTransactionItem(transaction)
            }
        }
    }
}

@Composable
fun CardTransactionItem(item: TransactionUIModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                if (item.formattedParcelInfo != null) {
                    Text(
                        text = "Parcela ${item.formattedParcelInfo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Total compra: ${item.formattedTotalAmount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.formattedAmount,
                    color = item.color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = item.formattedDate, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun CreditCardBarChart(
    data: List<CreditCardAmountByYear>,
    barColor: Color,
    totalMonth: String,
    totalOpenInvoices: String,
    modifier: Modifier = Modifier,
    onStatusClick: (Boolean) -> Unit
) {
    val maxValue = data.maxOfOrNull { it.totalValue }?.takeIf { it > 0 } ?: 1.0
    val selectedMonthData = data.find { it.isSelected }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL DA FATURA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = totalMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "FATURAS EM ABERTO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = totalOpenInvoices,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (selectedMonthData != null && selectedMonthData.totalValue > 0) {
                StatusCheckbox(
                    isPaid = selectedMonthData.isPaid,
                    onCheckChange = { onStatusClick(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                var animationTriggered by remember { mutableStateOf(false) }

                val targetHeight = (item.totalValue / maxValue).toFloat().coerceIn(0.00f, 0.7f)

                val animatedHeight by animateFloatAsState(
                    targetValue = if (animationTriggered) targetHeight else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "BarHeight"
                )

                LaunchedEffect(item.totalValue) {
                    animationTriggered = true
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(animatedHeight)
                            .width(15.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (item.isSelected) barColor
                                else barColor.copy(alpha = 0.2f)
                            )
                    )
                    Text(
                        text = item.monthName.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (item.isSelected) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}


@Composable
fun StatusCheckbox(
    isPaid: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckChange(!isPaid) }
            .background(if (isPaid) Color(0xFFE8F5E9) else Color.Transparent)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPaid) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (isPaid) Color(0xFF2E7D32) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isPaid) "Paga" else "Aberta",
            style = MaterialTheme.typography.labelMedium,
            color = if (isPaid) Color(0xFF2E7D32) else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}





