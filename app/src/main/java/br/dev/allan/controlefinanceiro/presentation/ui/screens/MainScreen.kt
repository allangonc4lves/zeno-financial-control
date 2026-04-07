package br.dev.allan.controlefinanceiro.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.room.TransactionScope
import br.dev.allan.controlefinanceiro.presentation.ui.components.ContentBarBox
import br.dev.allan.controlefinanceiro.presentation.ui.components.MyBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.MyTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.TransactionScreen.ExpenseScreen


@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
                MyTopBar()
            },
            bottomBar = {
                MyBottomAppBar({}, {}, {}, {}, {})
            },
            floatingActionButton = {
                // O FAB do Pix
                FloatingActionButton(
                    onClick = { /*...*/ },
                    containerColor = Color(0xFF66D3B1), // Verde água
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp) // Tamanho um pouco maior para o efeito
                        .offset(y = 50.dp), // O OFFSET EMPURRA PARA BAIXO
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Pix",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,

        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Box() { ContentBarBox()}
                ExpenseScreen()
            }

        }
    }
}


