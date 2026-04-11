package br.dev.allan.controlefinanceiro.presentation.ui.features.transaction_add.components

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import br.dev.allan.controlefinanceiro.domain.model.TransactionDirection

@Composable
fun CustomSingleChoiceSegmentedButton(
    selectedIncomeOrExpense: Int,
    onSelectionChange: (Int) -> Unit
) {
    val options = listOf("Entrada", "Saída")

    SingleChoiceSegmentedButtonRow {
        val selected = if (selectedIncomeOrExpense == 0) TransactionDirection.INCOME else TransactionDirection.EXPENSE
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