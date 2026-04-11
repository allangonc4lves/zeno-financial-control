package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components

import androidx.compose.animation.core.Animatable // Certifique-se de que é este!
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.presentation.ui.model.CategoryAppearance

@Composable
fun AnimatedDonutChart(
    data: Map<CategoryAppearance, Double>, // Categoria -> Valor
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().toFloat()
    val animationProgress = remember { Animatable(0f) }

    // Dispara a animação assim que o componente entra na tela
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier.size(120.dp).padding(8.dp)) {
        val strokeWidth = 30.dp.toPx()
        var startAngle = -90f

        data.forEach { (appearance, value) ->
            val sweepAngle = (value.toFloat() / total) * 360f

            drawArc(
                color = appearance.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle * animationProgress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}