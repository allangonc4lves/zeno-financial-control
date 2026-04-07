package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.domain.model.ButtunBarNavigation

@Composable
fun MyBottomAppBar(
    onAccountClick: () -> Unit,
    onCardsClick: () -> Unit,
    onPixClick: () -> Unit,
    onPaymentsClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val items = listOf(
        ButtunBarNavigation("Conta", Icons.Default.Search),
        ButtunBarNavigation("Cartões", Icons.Default.Search),
        ButtunBarNavigation("Pagamentos", Icons.Default.Search),
        ButtunBarNavigation("Mais", Icons.Default.Search)
    )

    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.graphicsLayer {
            clip = true
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        }
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Itens da Esquerda
            ButtunBarNavigation(items[0])
            ButtunBarNavigation(items[1], isSelected = true) // Exemplo de selecionado

            // Espaço para o FAB central (importante para não sobrepor ícones)
            Spacer(modifier = Modifier.width(48.dp))

            // Itens da Direita
            ButtunBarNavigation(items[2])
            ButtunBarNavigation(items[3])
        }
    }
}

@Composable
fun ButtunBarNavigation(item: ButtunBarNavigation, isSelected: Boolean = false) {
    val color = if (isSelected) Color(0xFF4A56E2) else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { /* Navegação */ }
    ) {
        // Indicador superior se estiver selecionado (a linha roxa do print)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Icon(item.icon, contentDescription = item.label, tint = color)
        Text(text = item.label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}