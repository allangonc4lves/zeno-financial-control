package br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.CreditCardPreviewType
import br.dev.allan.controlefinanceiro.domain.model.InputModeCustomTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.CreditCardPreview
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomOutlinedTextField
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.components.Loading
import br.dev.allan.controlefinanceiro.presentation.ui.components.ZenoDialog
import br.dev.allan.controlefinanceiro.presentation.ui.features.add_credit_card.SaveCreditCardUiEvent
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CreditCardsViewModel

@Composable
fun AddCreditCardDialog(
    cardId: String? = null,
    onDismiss: () -> Unit,
    viewModel: CreditCardsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) {
        if (cardId != null) {
            viewModel.loadCardToEdit(cardId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SaveCreditCardUiEvent.SaveSuccess -> {
                    onDismiss()
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Cartão?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeCard()
                    showDeleteConfirm = false
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ZenoDialog(
        onDismiss = { onDismiss() },
        onConfirm = { viewModel.saveCard() },
        content = {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.isLoading) 0.5f else 1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextTitle(if (cardId == null) "Novo Cartão" else "Editar Cartão")
                        if (cardId != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    CustomOutlinedTextField(
                        value = state.bankName,
                        label = "Banco*",
                        capitalization = KeyboardCapitalization.Sentences,
                        isError = state.bankNameError != null,
                        errorMessage = state.bankNameError ?: "",
                        onValueChange = { viewModel.onBankNameChange(it) }
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.6f),
                            value = state.brand,
                            label = "Bandeira*",
                            capitalization = KeyboardCapitalization.Sentences,
                            isError = state.brandError != null,
                            errorMessage = state.brandError ?: "",
                            onValueChange = { viewModel.onBrandChange(it) }
                        )
                        CustomOutlinedTextField(
                            modifier = Modifier.weight(0.4f),
                            value = state.lastDigits,
                            label = "Últimos digitos",
                            inputMode = InputModeCustomTextField.DIGITS,
                            maxLength = 4,
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.NumberPassword,
                            isError = state.lastDigitsError != null,
                            errorMessage = state.lastDigitsError ?: "",
                            onValueChange = {
                                viewModel.onLastDigitsChange(it)
                            }
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Selecione uma cor:")
                    ColorSelector(
                        palette = state.palette,
                        initialSelectedColor = state.backgroundColor,
                        onColorSelected = { viewModel.onColorSelected(it) }
                    )
                    Spacer(Modifier.height(4.dp))
                    CreditCardPreview(
                        bankName = state.bankName,
                        brand = state.brand,
                        lastDigits = state.lastDigits,
                        backgroundColorLong = state.backgroundColor,
                        previewType = CreditCardPreviewType.SMALL
                    )
                }

                if (state.isLoading) {
                    Loading()
                }
            }
        }
    )
}