package br.dev.allan.controlefinanceiro.presentation.ui.main.components

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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenoTopBar(
    onProfileClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val userName by viewModel.userName.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()

    TopAppBar(
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        navigationIcon = {
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
                    text = stringResource(id = R.string.hello_user, userName.ifBlank { "Usuário" }),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.search),
                        tint = Color.White
                    )
                }
            }
        }
    )
}