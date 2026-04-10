package br.dev.allan.controlefinanceiro.presentation.ui.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdown(
    selectedType: TransactionINorEX,
    selectedCategory: TransactionCategory?,
    onCategorySelected: (TransactionCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Filtra as categorias do Enum baseando-se no tipo (Income/Expense)
    // O 'remember(selectedType)' garante que a lista só mude se o tipo mudar
    val availableCategories = remember(selectedType) {
        TransactionCategory.entries.filter { it.getAppearance().type == selectedType }
    }

    val selectedAppearance = selectedCategory?.getAppearance()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedAppearance?.displayName ?: "Selecione a Categoria",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            leadingIcon = selectedAppearance?.let { appearance ->
                { Icon(appearance.icon, contentDescription = null) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                    text = { Text(appearance.displayName) },
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