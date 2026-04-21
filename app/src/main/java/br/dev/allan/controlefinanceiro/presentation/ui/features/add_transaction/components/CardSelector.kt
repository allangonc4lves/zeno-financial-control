package br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction.components

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.CreditCard
import br.dev.allan.controlefinanceiro.utils.constants.CreditCardPreviewType
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CardSelector(
    cards: List<CreditCard>,
    selectedCardId: String?,
    onCardSelected: (String?) -> Unit
) {
    if (cards.isEmpty()) {
        Text("Nenhum cartão cadastrado")
        return
    }

    val cardWidth = 220.dp
    val cardHeight = 140.dp
    val pageSpacing = 16.dp

    val startIndex = cards.indexOfFirst { it.id == selectedCardId }.coerceIn(0, cards.lastIndex)

    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2 + startIndex,
        pageCount = { Int.MAX_VALUE }
    )

    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val containerWidth = maxWidth
        val horizontalPadding = (containerWidth - cardWidth) / 2

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSize = PageSize.Fixed(cardWidth),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            pageSpacing = pageSpacing
        ) { page ->
            val card = cards[page % cards.size]

            val pageOffset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
            val scale = (1f - 0.08f * abs(pageOffset)).coerceIn(0.92f, 1f)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .width(cardWidth)
                    .height(cardHeight)
                    .clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                CreditCardPreview(
                    bankName = card.bankName,
                    brand = card.brand,
                    lastDigits = card.lastDigits.toString(),
                    backgroundColorLong = card.backgroundColor,
                    previewType = CreditCardPreviewType.SMALL
                )
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val card = cards[pagerState.currentPage % cards.size]
        onCardSelected(card.id)
    }
}
