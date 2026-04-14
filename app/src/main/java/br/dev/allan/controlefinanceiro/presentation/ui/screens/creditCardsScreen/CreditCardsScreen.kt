package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import br.dev.allan.controlefinanceiro.domain.model.CreditCard

@Composable
fun CreditCardsScreen(
    cards: List<CreditCard>,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 300.dp,
    cardHeight: Dp = 190.dp,
    onCardClick: (String) -> Unit = {}
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { cards.size }
    )

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        pageSize = PageSize.Fixed(cardWidth),
        contentPadding = PaddingValues(horizontal = (LocalConfiguration.current.screenWidthDp.dp - cardWidth) / 2),
        pageSpacing = 16.dp,
        beyondViewportPageCount = 2
    ) { page ->
        val card = cards[page]
        val scale = if (pagerState.currentPage == page) 1f else 0.92f

        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .width(cardWidth)
                .height(cardHeight)
                .clickable { onCardClick(card.id) },
            contentAlignment = Alignment.Center
        ) {
            CreditCardCanvas(
                bankName = card.bankName,
                brand = card.brand,
                backgroundColorLong = card.backgroundColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun CreditCardCanvas(
    bankName: String,
    brand: String,
    backgroundColorLong: Long,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 16.dp
    // Converte Long ARGB para Color do Compose
    val bgColor = Color(backgroundColorLong)

    // escolhe cor do texto com base na luminância para garantir contraste
    val contentColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .shadow(8.dp, RoundedCornerShape(cornerRadius))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // cria uma variação mais escura para o gradiente (multiplica por fator)
            fun darken(c: Color, factor: Float): Color {
                return Color(
                    red = (c.red * factor).coerceIn(0f, 1f),
                    green = (c.green * factor).coerceIn(0f, 1f),
                    blue = (c.blue * factor).coerceIn(0f, 1f),
                    alpha = c.alpha
                )
            }

            val darker = darken(bgColor, 0.85f)
            val gradient = Brush.linearGradient(
                colors = listOf(bgColor, darker),
                start = Offset(0f, 0f),
                end = Offset(w, h),
                tileMode = TileMode.Clamp
            )
            drawRoundRect(brush = gradient, cornerRadius = CornerRadius(24f, 24f), size = size, style = Fill)

            // chip
            val chipW = w * 0.18f
            val chipH = h * 0.14f
            drawRoundRect(
                color = Color(0xFFFFD54F),
                topLeft = Offset(w * 0.08f, h * 0.18f),
                size = Size(chipW, chipH),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // linhas decorativas
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(w * 0.06f, h * 0.75f),
                end = Offset(w * 0.94f, h * 0.75f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.12f),
                start = Offset(w * 0.06f, h * 0.82f),
                end = Offset(w * 0.94f, h * 0.82f),
                strokeWidth = 2f
            )

            // logo circular
            val logoRadius = h * 0.12f
            val logoCenterX = w * 0.86f
            val logoCenterY = h * 0.22f
            drawCircle(color = contentColor.copy(alpha = 0.18f), radius = logoRadius, center = Offset(logoCenterX, logoCenterY))

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = if (contentColor == Color.White) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    textSize = (logoRadius * 0.9f)
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    brand.firstOrNull()?.toString() ?: "B",
                    logoCenterX,
                    logoCenterY + (paint.textSize / 3f),
                    paint
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = brand, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Column {
                Text(text = "Credit Card", color = contentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "••••  ••••  ••••  1234", color = contentColor.copy(alpha = 0.95f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(text = bankName, color = contentColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


