package br.dev.allan.controlefinanceiro.presentation.ui.screens.reportsScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.presentation.ui.screens.transactionsScreen.TransactionItemRow
import br.dev.allan.controlefinanceiro.presentation.viewmodel.CreditCardTransactionViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.dev.allan.controlefinanceiro.presentation.ui.screens.creditCardsScreen.CreditCardUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    navController: NavHostController,
    viewModel: CreditCardTransactionViewModel = hiltViewModel()
) {

}

