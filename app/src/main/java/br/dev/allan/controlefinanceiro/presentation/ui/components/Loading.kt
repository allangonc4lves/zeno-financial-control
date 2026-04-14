package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Loading(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
    segmentStartColor: Color = MaterialTheme.colorScheme.primary,
    segmentEndColor: Color = MaterialTheme.colorScheme.secondary,
    strokeWidth: Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "expressive_loader")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val sweep by transition.animateFloat(
        initialValue = 20f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep"
    )

    val startOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "startOffset"
    )

    val colorProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorProgress"
    )
    val segmentColor = lerpColor(segmentStartColor, segmentEndColor, colorProgress)

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier
            .size(size)
            .rotate(rotation)
        ) {
            val stroke = strokeWidth.toPx()
            val diameter = size.toPx()
            val padding = stroke / 2f
            val arcSize = Size(diameter - stroke, diameter - stroke)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = segmentColor,
                startAngle = startOffset,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val clamped = fraction.coerceIn(0f, 1f)
    val r = start.red + (end.red - start.red) * clamped
    val g = start.green + (end.green - start.green) * clamped
    val b = start.blue + (end.blue - start.blue) * clamped
    val a = start.alpha + (end.alpha - start.alpha) * clamped
    return Color(r, g, b, a)
}