package br.dev.allan.controlefinanceiro.presentation.ui.screens.homeScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthSelectorMenu(
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var yearInMenu by remember(expanded) { mutableStateOf(selectedMonth.year) }

    val currentMonthName = remember(selectedMonth) {
        selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
            .replaceFirstChar { it.uppercase() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // 1. Removido o fillMaxWidth daqui para o container não ocupar a tela toda
        // e o menu não se perder na esquerda.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2. Usamos um Box para ser a "âncora" exata do menu
            Box(contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                ) {
                    CustomTextTitle(
                        text = "$currentMonthName ${selectedMonth.year}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                // O DropdownMenu agora sabe que deve abrir em relação ao Box centralizado
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    // Opcional: ajuste fino de posição se necessário
                    // offset = DpOffset(x = 0.dp, y = 4.dp),
                    modifier = Modifier
                ) {
                    // ... (seu conteúdo do menu: Cabeçalho, Divider e Column de meses)
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { yearInMenu-- }) {
                            Icon(Icons.Default.KeyboardArrowLeft, null, modifier = Modifier.size(18.dp))
                        }
                        CustomTextTitle(
                            text = yearInMenu.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { yearInMenu++ }) {
                            Icon(Icons.Default.KeyboardArrowRight, null, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Month.entries.forEach { month ->
                            val isSelected = month == selectedMonth.month && yearInMenu == selectedMonth.year
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                                            .replaceFirstChar { it.uppercase() },
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onMonthChange(YearMonth.of(yearInMenu, month))
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}