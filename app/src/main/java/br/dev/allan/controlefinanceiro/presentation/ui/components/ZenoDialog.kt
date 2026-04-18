package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenoDialog(
    dismissText: String = "Cancelar",
    confirmText: String = "Salvar",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        //title = { CustomTextTitle(text = title, color = MaterialTheme.colorScheme.onPrimaryContainer) },
        text = {
            content()
        },
        confirmButton = { Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) { CustomTextContent(text = confirmText, color = MaterialTheme.colorScheme.background)
        }
        },
        dismissButton = { TextButton(onClick = onDismiss) { CustomTextContent(text = dismissText) } }
    )
}