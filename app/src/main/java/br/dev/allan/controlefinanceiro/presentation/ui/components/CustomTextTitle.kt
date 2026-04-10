package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextTitle(
    text: String,
    color: Color,
    startPadding: Int = 0
) {
    Text(modifier = Modifier.padding(startPadding.dp),
    text = text,
    color = color,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    style = MaterialTheme.typography.titleMedium)
}