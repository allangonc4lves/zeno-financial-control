package br.dev.allan.controlefinanceiro.presentation.ui.main.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ZenoDrawBoxTop(content: @Composable () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val drawingHeightDp = screenHeight * 0.1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // 2. Convertemos os 20% da altura da tela para Pixels
                val drawingHeightPx = drawingHeightDp.toPx()
                val cornerRadiusPx = 40.dp.toPx()

                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)

                    // Usamos drawingHeightPx em vez de size.height para o desenho
                    lineTo(size.width, drawingHeightPx - cornerRadiusPx)

                    arcTo(
                        rect = Rect(
                            left = size.width - 2 * cornerRadiusPx,
                            top = drawingHeightPx - 2 * cornerRadiusPx,
                            right = size.width,
                            bottom = drawingHeightPx
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    lineTo(cornerRadiusPx, drawingHeightPx)

                    arcTo(
                        rect = Rect(
                            left = 0f,
                            top = drawingHeightPx - 2 * cornerRadiusPx,
                            right = 2 * cornerRadiusPx,
                            bottom = drawingHeightPx
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }

                drawPath(
                    path = path,
                    color = primaryColor
                )
            },
    ) {
        content()
    }
}