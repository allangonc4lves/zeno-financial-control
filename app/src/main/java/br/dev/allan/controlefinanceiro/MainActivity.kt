package br.dev.allan.controlefinanceiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.dev.allan.controlefinanceiro.presentation.ui.main.MainScreen
import br.dev.allan.controlefinanceiro.presentation.ui.theme.ControleFinanceiroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControleFinanceiroTheme {
                MainScreen()
            }
        }
    }
}