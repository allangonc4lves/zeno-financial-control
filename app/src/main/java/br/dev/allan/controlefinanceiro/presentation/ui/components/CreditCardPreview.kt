package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.utils.constants.CreditCardPreviewType

@Composable
fun CreditCardPreview(
    bankName: String,
    brand: String,
    lastDigits: String,
    modifier: Modifier = Modifier,
    backgroundColorLong: Long,
    previewType: CreditCardPreviewType = CreditCardPreviewType.DEFAULT,
) {
    val cornerRadius = 16.dp
    val bgColor = Color(backgroundColorLong)
    val contentColor = if (bgColor.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White

    var w = 250.dp
    var h = 160.dp
    var bankNameFontSize = 18.sp
    var brandFontSize = 16.sp
    var digitsFontSize = 20.sp

    if (previewType == CreditCardPreviewType.SMALL) {
        w = 220.dp
        h = 140.dp
        bankNameFontSize = 14.sp
        brandFontSize = 14.sp
        digitsFontSize = 11.sp
    }

    Box(
        modifier = modifier
            .width(w)
            .height(h)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            val gradient = Brush.linearGradient(
                colors = listOf(
                    bgColor,
                    Color(
                        red = (bgColor.red * 0.7f).coerceIn(0f, 1f),
                        green = (bgColor.green * 0.7f).coerceIn(0f, 1f),
                        blue = (bgColor.blue * 0.7f).coerceIn(0f, 1f),
                        alpha = 1f
                    )
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            drawRoundRect(brush = gradient, size = size)

            val sheenGradient = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f)
                ),
                start = Offset(w * 0.2f, 0f),
                end = Offset(w * 0.8f, h)
            )
            drawRoundRect(brush = sheenGradient, size = size)

            val chipW = w * 0.16f
            val chipH = h * 0.13f
            val chipX = w * 0.08f
            val chipY = h * 0.22f

            drawRoundRect(
                color = Color(0xFFEBC045),
                topLeft = Offset(chipX, chipY),
                size = Size(chipW, chipH),
                cornerRadius = CornerRadius(8f, 8f)
            )

            val stroke = 1.5f
            val chipColorDark = Color(0xFFB98D15)
            drawRect(
                color = chipColorDark,
                topLeft = Offset(chipX + (chipW * 0.3f), chipY),
                size = Size(stroke, chipH),
                style = Fill
            )
            drawRect(
                color = chipColorDark,
                topLeft = Offset(chipX + (chipW * 0.6f), chipY),
                size = Size(stroke, chipH),
                style = Fill
            )
            drawRect(
                color = chipColorDark,
                topLeft = Offset(chipX, chipY + (chipH * 0.5f)),
                size = Size(chipW, stroke),
                style = Fill
            )

            drawPath(
                path = Path().apply {
                    moveTo(0f, h * 0.8f)
                    quadraticBezierTo(w * 0.5f, h * 0.75f, w, h * 0.9f)
                },
                color = Color.White.copy(alpha = 0.08f),
                style = Stroke(width = 40f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = brand.uppercase(),
                    color = contentColor.copy(alpha = 0.9f),
                    fontSize = brandFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Column {
                Text(
                    text = stringResource(id = R.string.credit_card_label).uppercase(),
                    color = contentColor.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "••••  ••••  •••• $lastDigits",
                    color = contentColor,
                    fontSize = digitsFontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }

            Text(
                text = bankName,
                color = contentColor,
                fontSize = bankNameFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

