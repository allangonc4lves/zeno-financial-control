package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateHeader(dateMillis: Long, modifier: Modifier = Modifier) {
    val date = Date(dateMillis)
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        .replaceFirstChar { it.uppercase() }
    val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(date)

    Column(modifier = modifier.padding(top = 8.dp, bottom = 4.dp, start = 16.dp)) {
        Text(
            text = "$dayOfWeek, $dayOfMonth",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        )
    }
}
