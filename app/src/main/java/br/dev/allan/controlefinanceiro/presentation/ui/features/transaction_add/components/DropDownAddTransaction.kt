package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components


import android.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownAddTransaction(
    selectedType: TransactionDirection,
    selectedCategory: TransactionCategory?,
    onCategorySelected: (TransactionCategory) -> Unit,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    val availableCategories = remember(selectedType) {
        TransactionCategory.entries.filter { it.getAppearance().type == selectedType }
    }

    val selectedAppearance = selectedCategory?.getAppearance()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        CustomOutlinedTextField(
            value = selectedAppearance?.displayName ?: "Selecione a Categoria",
            onValueChange = {},
            isReadOnly = true,
            label = "Categoria*",
            isError = isError,
            errorMessage = errorMessage,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = selectedAppearance?.let { appearance ->
                { Icon(appearance.icon, contentDescription = null) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableCategories.forEach { category ->
                val appearance = category.getAppearance()
                DropdownMenuItem(
                    text = {
                        CustomTextContent(
                            appearance.displayName,
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = appearance.icon,
                            contentDescription = appearance.displayName
                        )
                    }
                )
            }
        }
    }
}