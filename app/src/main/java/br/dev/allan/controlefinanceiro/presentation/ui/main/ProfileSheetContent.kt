package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.viewmodel.LoginViewModel
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel
import coil3.compose.AsyncImage

@Composable
fun ProfileSheetContent(
    onClose: () -> Unit,
    viewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    onLogout: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    val context = LocalContext.current
    
    var showCurrencySelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        // Cabeçalho com informações do usuário
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.user_without_img),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = userName.ifBlank { stringResource(id = R.string.user_default) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail.ifBlank { stringResource(id = R.string.no_email) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

        if (!showCurrencySelector) {
            // Menu Principal
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            MenuItem(
                icon = Icons.Default.CurrencyExchange,
                title = stringResource(id = R.string.main_currency),
                subtitle = stringResource(id = R.string.change_system_currency),
                onClick = { showCurrencySelector = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MenuItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = stringResource(id = R.string.logout),
                subtitle = stringResource(id = R.string.logout_description),
                textColor = MaterialTheme.colorScheme.error,
                iconColor = MaterialTheme.colorScheme.error,
                onClick = {
                    loginViewModel.logout(context) {
                        onLogout()
                    }
                }
            )
        } else {
            // Seletor de Moedas (Submenu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.select_currency),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(id = R.string.back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showCurrencySelector = false }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            val currentCurrency by viewModel.currencyCode.collectAsState(initial = "BRL")
            val currencies = listOf(
                CurrencyOption("BRL", stringResource(id = R.string.brazilian_real), "pt-BR"),
                CurrencyOption("USD", stringResource(id = R.string.us_dollar), "en-US"),
                CurrencyOption("EUR", stringResource(id = R.string.euro), "de-DE"),
                CurrencyOption("ARS", stringResource(id = R.string.argentine_peso), "es-AR")
            )

            currencies.forEach { option ->
                CurrencyItem(
                    option = option,
                    isSelected = currentCurrency == option.code,
                    onClick = {
                        viewModel.updateCurrency(option.code)
                        showCurrencySelector = false
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
fun CurrencyItem(
    option: CurrencyOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = option.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = option.code,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

data class CurrencyOption(val code: String, val name: String, val locale: String)