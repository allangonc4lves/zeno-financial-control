package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItemRow(
    item: Transaction
) {
    // Pegamos a aparência através da função de extensão que criamos
    val appearance = item.category.getAppearance()
    val isExpense = item.type == TransactionINorEX.EXPENSE
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = appearance.icon,
            contentDescription = appearance.displayName,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.subtitle1)
            Text(appearance.displayName, style = MaterialTheme.typography.body2, color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isExpense) "- " else "+ ") + "R$ ${String.format("%.2f", item.amount)}",
                style = MaterialTheme.typography.subtitle1,
                color = if (isExpense) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
            Text(
                text = dateFormatter.format(Date(item.date)),
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }
    }
}