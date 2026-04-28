package br.dev.allan.controlefinanceiro.presentation.ui.main.components

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.domain.model.ButtonAppBarNavigation
import br.dev.allan.controlefinanceiro.presentation.viewmodel.NavigationViewModel
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.HomeRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.ReportsRoute
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.CreditCardsRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import br.dev.allan.controlefinanceiro.presentation.viewmodel.MainViewModel
import coil3.compose.AsyncImage

@Composable
fun ZenoBottomAppBar(
    navController: NavHostController,
    onProfileClick: () -> Unit,
    viewModel: NavigationViewModel = hiltViewModel(),
    @SuppressLint("ContextCastToActivity") mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val userPhotoUrl by mainViewModel.userPhotoUrl.collectAsState()
    val userName by mainViewModel.userName.collectAsState()

    val buttonsAppBarList = listOf(
        ButtonAppBarNavigation(stringResource(R.string.nav_home), Icons.Rounded.Home, HomeRoute),
        ButtonAppBarNavigation(stringResource(R.string.nav_reports), Icons.Rounded.Analytics, ReportsRoute),
        ButtonAppBarNavigation(stringResource(R.string.nav_credit_cards), Icons.Rounded.CreditCard, CreditCardsRoute)
    )

    val itemPositions = remember { mutableStateMapOf<Int, androidx.compose.ui.geometry.Offset>() }
    val density = LocalDensity.current

    val selectedIndex = buttonsAppBarList.indexOfFirst { item ->
        currentDestination?.hasRoute(item.route::class) ?: false
    }

    val targetPosition = itemPositions[selectedIndex] ?: androidx.compose.ui.geometry.Offset.Zero
    val animatedX by animateFloatAsState(
        targetValue = targetPosition.x,
        animationSpec = spring(stiffness = 500f),
        label = "IndicatorAnimationX"
    )
    val animatedY by animateFloatAsState(
        targetValue = targetPosition.y,
        animationSpec = spring(stiffness = 500f),
        label = "IndicatorAnimationY"
    )

    val isNearTarget = kotlin.math.abs(animatedX - targetPosition.x) < 2f
    val animatedScale by animateFloatAsState(
        targetValue = if (isNearTarget) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = if (isNearTarget) Spring.DampingRatioLowBouncy else Spring.DampingRatioNoBouncy,
            stiffness = if (isNearTarget) Spring.StiffnessLow else Spring.StiffnessMedium
        ),
        label = "IndicatorScale"
    )

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.9f),
                spotColor = Color.Transparent
            )
    ) {
        var boxCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { boxCoords = it }
        ) {
            if (selectedIndex != -1 && targetPosition != androidx.compose.ui.geometry.Offset.Zero) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { (animatedX - (14.dp.toPx())).toDp() },
                            y = with(density) { (animatedY - (14.dp.toPx())).toDp() }
                        )
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                            shape = CircleShape
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttonsAppBarList.forEachIndexed { index, item ->
                    if (index == 2) Spacer(modifier = Modifier.width(48.dp))

                    val isSelected = selectedIndex == index

                    ZenoButtonBarNavigation(
                        item = item,
                        isSelected = isSelected,
                        modifier = Modifier,
                        iconModifier = Modifier.onGloballyPositioned { iconCoords ->
                            boxCoords?.let { parent ->
                                val parentPos = parent.positionInWindow()
                                val iconPos = iconCoords.positionInWindow()
                                RelativeX(iconPos.x - parentPos.x)
                                val relativeY = iconPos.y - parentPos.y
                                
                                itemPositions[index] = androidx.compose.ui.geometry.Offset(
                                    x = iconPos.x - parentPos.x + (iconCoords.size.width / 2f),
                                    y = relativeY + (iconCoords.size.height / 2f)
                                )
                            }
                        },
                        onClick = { viewModel.navigateWithOptions(navController, item.route) }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onProfileClick() }
                        .padding(4.dp)
                        .size(70.dp, 48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userPhotoUrl != null) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = stringResource(id = R.string.user_image),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.user_without_img),
                                contentDescription = stringResource(id = R.string.user_image),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Text(
                        text = userName.ifBlank { "Usuário" }.trim().substringBefore(" "),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun RelativeX(f: Float) {}
