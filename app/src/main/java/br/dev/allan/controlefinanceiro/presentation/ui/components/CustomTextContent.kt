package br.dev.allan.controlefinanceiro.presentation.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextContent(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    startPadding: Int = 0,
    endPadding: Int = 0,
) {
    Text(modifier = modifier
        .padding(start = startPadding.dp, end = endPadding.dp ),
    text = text,
    color = color,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
}