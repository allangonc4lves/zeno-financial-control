package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.R
import kotlinx.coroutines.launch

@Composable
fun ContentBarBox() {

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .drawBehind {
                val cornerRadius = 40f

                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height / 1.5f)
                    arcTo(
                        rect = Rect(
                            left = size.width - 2 * cornerRadius,
                            top = size.height / 2 - cornerRadius,
                            right = size.width,
                            bottom = size.height / 2 + cornerRadius
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(cornerRadius, size.height / 1.5f)
                    arcTo(
                        rect = Rect(
                            left = 0f,
                            top = size.height / 2 - cornerRadius,
                            right = 2 * cornerRadius,
                            bottom = size.height / 2 + cornerRadius
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
                /*
                                                                        drawRoundRect(
                                                                        color = Color(0xFF0B1F3C),
                                                                        size = Size(size.width, size.height / 1.7f),
                                                                        cornerRadius = CornerRadius(x = 32f, y = 32f)
                                                                        )

                                                                        drawRoundRect(
                                                                        color = Color(0xFF0B1F3C),
                                                                        size = Size(size.width, size.height / 1.7f),
                                                                        cornerRadius = CornerRadius(x = 32f, y = 32f)
                                                                        )
               */
            },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(start = 12.dp, end = 12.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Janeiro")
            }
        }
    }
}