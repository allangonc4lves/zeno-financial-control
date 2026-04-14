package br.dev.allan.controlefinanceiro.domain.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val bankName: String,
    val brand: String,
    val lastDigits: Int,
    val backgroundColor: Long = 0xFF1E88E5
)