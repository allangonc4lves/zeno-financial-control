package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import br.dev.allan.controlefinanceiro.presentation.ui.model.AddTransactionINorEX

@Composable
fun CustomSingleChoiceSegmentedButton(
    selectedIncomeOrExpense: Int,
    onSelectionChange: (Int) -> Unit
) {
    val options = listOf("Entrada", "Saída")

    SingleChoiceSegmentedButtonRow {
        val selected = if (selectedIncomeOrExpense == 0) AddTransactionINorEX.INCOME else AddTransactionINorEX.EXPENSE
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onSelectionChange(index) },
                selected = index == selectedIncomeOrExpense,
                label = { Text(label) }
            )
        }
    }
}