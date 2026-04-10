package br.dev.allan.controlefinanceiro.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import br.dev.allan.controlefinanceiro.presentation.ui.model.getAppearance
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomCard
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.DrawBoxTop
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomFabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextContent
import br.dev.allan.controlefinanceiro.presentation.ui.components.CustomTextTitle
import br.dev.allan.controlefinanceiro.presentation.ui.theme.ControleFinanceiroTheme
import br.dev.allan.controlefinanceiro.presentation.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val transaction by viewModel.transactions.collectAsState()

    ControleFinanceiroTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Menu lateral", modifier = Modifier.padding(16.dp))
                    Text("Configurações", modifier = Modifier.padding(16.dp))
                    Text("Perfil", modifier = Modifier.padding(16.dp))
                    Text("Sair", modifier = Modifier.padding(16.dp))
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CustomTopBar()
                },
                bottomBar = {
                    CustomBottomAppBar({}, {}, {}, {}, {})
                },
                floatingActionButton = {
                    CustomFabBottomBar()
                },
                floatingActionButtonPosition = FabPosition.Center,

                ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    item {
                        DrawBoxTop {
                            CustomCard {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.size(16.dp))
                        CustomTextTitle("Últimas atividades", Color.Black, 16)
                    }


                    items(transaction) { item ->
                        val appearance = item.category.getAppearance()

                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = appearance.icon,
                                    contentDescription = appearance.displayName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .width(32.dp)
                                )
                                Column() {
                                    CustomTextTitle(item.title, MaterialTheme.colorScheme.onSurfaceVariant)
                                    CustomTextContent(appearance.displayName, MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                CustomTextTitle(if (item.type == TransactionINorEX.EXPENSE) "- " + "R$ ${item.amount}" else "+ " + "R$ ${item.amount}", Color.Black)
                                CustomTextContent(
                                    SimpleDateFormat(
                                        "dd/MM/yyyy", Locale.getDefault()).format(Date(item.date)
                                ), MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(thickness = 2.dp)
                    }
                }
            }
        }
    }
}