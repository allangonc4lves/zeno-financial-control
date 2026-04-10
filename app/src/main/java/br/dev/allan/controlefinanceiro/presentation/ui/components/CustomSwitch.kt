package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CustomSwitch(
    text: String,
    quantityValue: Int,
    onQuantityChange: (Int) -> Unit,
    showQuantity: Boolean = false,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CustomTextContent(text = text, color = MaterialTheme.colorScheme.primary)

        if (showQuantity) {
            OutlinedTextField(
                value = if (quantityValue == 0) "" else quantityValue.toString(),
                onValueChange = { newValue ->
                    if (newValue.isEmpty()) {
                        onQuantityChange(0)
                    } else {
                        newValue.toIntOrNull()?.let { onQuantityChange(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                leadingIcon = {
                    IconButton(onClick = { if (quantityValue > 2) onQuantityChange(quantityValue - 1) }) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Diminuir",
                            modifier = Modifier.size(14.dp))
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { if (quantityValue < 360) onQuantityChange(quantityValue + 1) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(14.dp))
                    }
                },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(32.dp),
                singleLine = true
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Outlined.Check else Icons.Outlined.Stream,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                    tint = Color.White
                )
            }
        )
    }
}