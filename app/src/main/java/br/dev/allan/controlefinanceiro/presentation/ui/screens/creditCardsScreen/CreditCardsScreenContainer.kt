package br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlin.random.Random


@Composable
fun CreditCardsScreenContainer(
    viewModel: CardsViewModel = hiltViewModel(),
    onCardClick: (String) -> Unit = {}
) {
    val cardsState = viewModel.cards.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val randomColor = 0xFF000000L or (kotlin.random.Random.nextInt(0x00FFFFFF).toLong() and 0x00FFFFFFL)
                viewModel.addCard(bankName = "Novo Banco", brand = "Visa", backgroundColor = randomColor)
            }) { Text("+") }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            CreditCardsScreen(cards = cardsState.value, modifier = Modifier.fillMaxSize(), onCardClick = onCardClick)
        }
    }
}