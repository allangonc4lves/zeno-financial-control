package br.dev.allan.controlefinanceiro.presentation.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.ButtonAppBarNavigation

@Composable
fun ZenoButtonBarNavigation(
    item: ButtonAppBarNavigation,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val colorIcon = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondary
    val colorText = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .size(70.dp, 60.dp)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = colorIcon,
            modifier = iconModifier.size(32.dp)
        )
        Text(
            text = item.label,
            color = colorText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
