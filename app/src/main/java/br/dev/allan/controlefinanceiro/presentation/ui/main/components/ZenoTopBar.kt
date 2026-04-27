package br.dev.allan.controlefinanceiro.presentation.ui.main.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.ImeAction
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.TransactionsRoute
import androidx.navigation.NavHostController
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenoTopBar(
    onProfileClick: () -> Unit,
    navController: NavController,
    @SuppressLint("ContextCastToActivity") mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val userName by mainViewModel.userName.collectAsState()
    val userPhotoUrl by mainViewModel.userPhotoUrl.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTransactionsScreen = currentDestination?.hasRoute<TransactionsRoute>() == true

    val isSearchMode by mainViewModel.isSearchMode.collectAsState()
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val searchOriginRoute by mainViewModel.searchOriginRoute.collectAsState()

    LaunchedEffect(currentDestination) {
        if (!isTransactionsScreen) {
            mainViewModel.setSearchMode(false)
            mainViewModel.onSearchQueryChange("")
        }
    }

    TopAppBar(
        title = {
            if (isSearchMode && isTransactionsScreen) {
                TextField(
                    value = searchQuery,
                    onValueChange = { mainViewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Pesquisar...", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    trailingIcon = {
                        IconButton(onClick = { 
                            mainViewModel.setSearchMode(false)
                            mainViewModel.onSearchQueryChange("")

                            searchOriginRoute?.let { origin ->
                                if (navController is NavHostController) {
                                    navigationViewModel.navigateWithOptions(navController, origin)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        navigationIcon = {
            if (!isSearchMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (userPhotoUrl != null) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = stringResource(id = R.string.user_image),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { coroutineScope.launch { onProfileClick() } },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.user_without_img),
                                contentDescription = stringResource(id = R.string.user_image),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { coroutineScope.launch { onProfileClick() } }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(id = R.string.hello_user, userName.ifBlank { "Usuário" }.trim().substringBefore(" ")),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { 
                        if (!isTransactionsScreen) {
                            // Detecta a origem dinamicamente
                            val origin = navigationViewModel.getRouteFromDestination(currentDestination)
                            mainViewModel.setSearchMode(true, origin ?: HomeRoute)
                            
                            if (navController is NavHostController) {
                                navigationViewModel.navigateWithOptions(navController, TransactionsRoute)
                            }
                        } else {
                            mainViewModel.setSearchMode(true)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    )
}
