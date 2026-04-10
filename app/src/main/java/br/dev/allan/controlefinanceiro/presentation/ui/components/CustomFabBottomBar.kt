package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomFabBottomBar(){
    var showDialog by remember { mutableStateOf(false) }

    FloatingActionButton(
    onClick = { showDialog = !showDialog },
    containerColor = Color(0xFF66D3B1),
    shape = CircleShape,
    modifier = Modifier
        .size(64.dp)
        .offset(y = 60.dp),
    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Pix",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }

    if (showDialog) {
        TransactionDialogAdd(
            onDismiss = { showDialog = false },
            onConfirm = { transaction ->
                showDialog = false
            }
        )
    }
}

