package br.dev.allan.controlefinanceiro.presentation.ui.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import br.dev.allan.controlefinanceiro.R
import br.dev.allan.controlefinanceiro.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "logo_scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.positive2),
                contentDescription = "Zeno Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.welcome_to_zeno),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedButton(
                onClick = { viewModel.signInWithGoogle(context, onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF131314),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF8E918F))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }

        Text(
            text = stringResource(id = R.string.developed_by),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
