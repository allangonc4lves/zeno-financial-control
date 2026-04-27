package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.domain.model.CreditCardAmountByYear
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components.ExpensesByCategoryCard
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.AddCreditCardRoute
import br.dev.allan.controlefinanceiro.presentation.ui.components.DateHeader
import br.dev.allan.controlefinanceiro.presentation.ui.components.TransactionItemRow
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.MonthSelector
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomCard
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CreditCardTransactionViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import br.dev.allan.controlefinanceiro.utils.formatMillisToMonthYear
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
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
    val categoryChartValues by viewModel.categoryChartData.collectAsState()
    val categoryChartLabels by viewModel.categoryChartLabels.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showInvoiceSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val pagerState = rememberPagerState(pageCount = { cards.size })

    val selectedCard = if (cards.isNotEmpty()) cards[pagerState.currentPage] else null
    val selectedCardColor =
        if (selectedCard != null) Color(selectedCard.backgroundColor) else Color.Gray

    LaunchedEffect(pagerState.currentPage, cards) {
        if (cards.isNotEmpty()) {
            viewModel.setSelectedCard(cards[pagerState.currentPage].id)
        }
    }

    val cardWidth: Dp = 300.dp

    val density = LocalDensity.current
    val pagerHeight = 220.dp
    val pagerHeightPx = with(density) { pagerHeight.toPx() }
    var pagerOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = pagerOffsetHeightPx + delta
                val consumed = if (delta < 0) { // Scrolling down (content moves up)
                    val oldOffset = pagerOffsetHeightPx
                    pagerOffsetHeightPx = newOffset.coerceIn(-pagerHeightPx, 0f)
                    pagerOffsetHeightPx - oldOffset
                } else {
                    0f
                }
                return Offset(0f, consumed)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta > 0) { // Scrolling up (content moves down)
                    val oldOffset = pagerOffsetHeightPx
                    pagerOffsetHeightPx = (pagerOffsetHeightPx + delta).coerceIn(-pagerHeightPx, 0f)
                    return Offset(0f, pagerOffsetHeightPx - oldOffset)
                }
                return Offset.Zero
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if(cards.isNotEmpty()){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pagerHeight + with(density) { pagerOffsetHeightPx.toDp() })
                        .graphicsLayer {
                            val progress = (1f + (pagerOffsetHeightPx / pagerHeightPx)).coerceIn(0f, 1f)
                            alpha = progress
                            scaleY = 0.8f + (0.2f * progress)
                        },
                    contentAlignment = Alignment.Center
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
                                .width(cardWidth)
                                .height(190.dp)
                                .clickable {
                                    navViewModel.navigateToForm(
                                        navController = navController,
                                        route = AddCreditCardRoute(id = card.id)
                                    )
                                }
                        )
                    }
                }

                MonthSelector(
                    currentMonthMillis = currentMonth,
                    onPreviousMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) }
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item {
                        if (chartData.isNotEmpty()) {
                            CreditCardBarChart(
                                data = chartData,
                                barColor = selectedCardColor,
                                totalMonth = selectedMonthTotal,
                                totalOpenInvoices = totalOpenInvoices,
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                onStatusClick = { isPaid ->
                                    viewModel.markMonthAsPaid(isPaid)
                                },
                                onCardClick = {
                                    showInvoiceSheet = true
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomTextTitle(
                            text = stringResource(R.string.spending_by_category_on_card),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            startPadding = 8
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExpensesByCategoryCard(
                            chartDataValues = categoryChartValues,
                            chartDataLabels = categoryChartLabels
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.zeno_not_found_cards),
                    contentDescription = "zeno",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Inside
                )
                CustomTextContent(
                    text = stringResource(R.string.no_card_found),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        if (showInvoiceSheet && selectedCard != null) {
            val selectedMonthData = chartData.find { it.isSelected }
            
            ModalBottomSheet(
                onDismissRequest = { showInvoiceSheet = false },
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
                            color = selectedCardColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = selectedCardColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedCard.bankName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.invoice_from, formatMillisToMonthYear(currentMonth)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val isPaid = selectedMonthData?.isPaid ?: false
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPaid) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color(0xFFAB1A1A).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (isPaid) Color(0xFF1B5E20) else Color(0xFFAB1A1A),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPaid) stringResource(R.string.paid) else stringResource(R.string.pending_payment),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMonthData?.isPaid == true) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
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
                        
                        val groupedTransactions = transactions.groupBy { it.formattedDate }
                        groupedTransactions.forEach { (date, txs) ->
                            item {
                                DateHeader(
                                    dateMillis = txs.first().dateMillis,
                                    modifier = Modifier.padding(start = 0.dp)
                                )
                            }
                            items(txs) { tx ->
                                TransactionItemRow(
                                    uiModel = tx,
                                    isAmountVisible = true,
                                    onClick = { }
                                )
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.total_invoice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedMonthTotal,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (selectedMonthData?.isPaid == true) Color(0xFF1B5E20) else Color(0xFFAB1A1A)
                                )
                            }
                        }
                    }
                }
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
    onStatusClick: (Boolean) -> Unit,
    onCardClick: () -> Unit
) {
    val maxValue = data.maxOfOrNull { it.totalValue }?.takeIf { it > 0 } ?: 1.0
    val selectedMonthData = data.find { it.isSelected }

    CustomCard() {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onCardClick() }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.total_invoice_uppercase),
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
                        text = stringResource(R.string.pending_filter).uppercase(),
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
            text = if (isPaid) stringResource(R.string.paid) else stringResource(R.string.pending_filter),
            style = MaterialTheme.typography.labelMedium,
            color = if (isPaid) Color(0xFF2E7D32) else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}
