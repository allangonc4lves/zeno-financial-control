package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.TransactionUIModel
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.MonthSelector
import br.dev.allan.controlefinanceiro.presentation.viewmodel.AddCreditCardsViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CreditCardTransactionViewModel
import br.dev.allan.controlefinanceiro.util.CurrencyManager

@Composable
fun CreditCardsScreen(
    viewModel: CreditCardTransactionViewModel = hiltViewModel(),
) {
    val cards by viewModel.cards.collectAsState()
    val transactions by viewModel.transactionsUiState.collectAsState()
    val chartData by viewModel.chartDataState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    val pagerState = rememberPagerState(pageCount = { cards.size })

    val selectedCardColor = if (cards.isNotEmpty()) Color(cards[pagerState.currentPage].backgroundColor) else Color.Gray

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
                modifier = Modifier.fillMaxWidth().height(190.dp)
            )
        }

        if (chartData.isNotEmpty()) {
            CreditCardBarChart(
                data = chartData,
                barColor = selectedCardColor,
                currencyManager = viewModel.currencyManager,
                modifier = Modifier.fillMaxWidth(),
                onStatusClick = { isPaid ->
                    viewModel.markMonthAsPaid(isPaid)
                }
            )
        }

        MonthSelector(
            currentMonthMillis = currentMonth,
            onPreviousMonth = { viewModel.changeMonth(-1) },
            onNextMonth = { viewModel.changeMonth(1) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
    currencyManager: CurrencyManager,
    modifier: Modifier = Modifier,
    onStatusClick: (Boolean) -> Unit
) {
    val maxValue = data.maxOfOrNull { it.totalValue }?.takeIf { it > 0 } ?: 1.0
    val selectedMonthData = data.find { it.isSelected }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // --- CABEÇALHO COM CHECKBOX ---
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
                    text = currencyManager.formatByCurrencyCode(selectedMonthData?.totalValue ?: 0.0, "BRL"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }

            // O Checkbox/Status na frente do título/valor
            if (selectedMonthData != null && selectedMonthData.totalValue > 0) {
                StatusCheckbox(
                    isPaid = selectedMonthData.isPaid,
                    onCheckChange = { onStatusClick(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val barHeightFraction = (item.totalValue / maxValue).toFloat()

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(barHeightFraction.coerceIn(0.05f, 1f))
                            .width(10.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (item.isSelected) barColor
                                else barColor.copy(alpha = 0.2f)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
fun StatusBadge(isPaid: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isPaid) Color(0xFF2E7D32) else Color(0xFFEF6C00).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isPaid) Color(0xFF2E7D32) else Color(0xFFEF6C00)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPaid) "PAGO" else "MARCAR PAGO",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFEF6C00)
            )
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
        // Ícone de Checkbox
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





