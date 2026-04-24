package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel

@Composable
fun ProfileSheetContent(
    onClose: () -> Unit,
    viewModel: MainViewModel
) {
    val currentCurrency by viewModel.currencyCode.collectAsState(initial = "BRL")

    val currencies = listOf(
        CurrencyOption("BRL", stringResource(id = R.string.brazilian_real), "pt-BR"),
        CurrencyOption("USD", stringResource(id = R.string.us_dollar), "en-US"),
        CurrencyOption("EUR", stringResource(id = R.string.euro), "de-DE"),
        CurrencyOption("ARS", stringResource(id = R.string.argentine_peso), "es-AR")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.select_main_currency),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        currencies.forEach { option ->
            CurrencyItem(
                option = option,
                isSelected = currentCurrency == option.code,
                onClick = {
                    viewModel.updateCurrency(option.code)
                    onClose()
                }
            )
        }
    }
}

@Composable
fun CurrencyItem(
    option: CurrencyOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = option.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = option.code,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

data class CurrencyOption(val code: String, val name: String, val locale: String)