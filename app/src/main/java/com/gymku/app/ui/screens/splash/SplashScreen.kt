package com.gymku.app.ui.screens.splash

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymku.app.data.preferences.AppPreferences
import com.gymku.app.ui.theme.IndigoMain
import com.gymku.app.ui.theme.Slate900
import com.gymku.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SplashScreen(
    onFirebaseNotConfigured: () -> Unit,
    onNotLoggedIn: () -> Unit,
    onAlreadyLoggedIn: () -> Unit
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800), label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        navigateFromSplash(context, onFirebaseNotConfigured, onNotLoggedIn, onAlreadyLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            // Logo placeholder — big bold letters
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(IndigoMain, androidx.compose.foundation.shape.RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "GymKu",
                color = White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manajemen Gym Modern",
                color = White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

private suspend fun navigateFromSplash(
    context: Context,
    onFirebaseNotConfigured: () -> Unit,
    onNotLoggedIn: () -> Unit,
    onAlreadyLoggedIn: () -> Unit
) {
    val prefs = AppPreferences(context)
    val config = prefs.getFirebaseConfig().firstOrNull()
    if (config == null || !config.isValid()) {
        onFirebaseNotConfigured()
        return
    }
    val admin = prefs.getLoggedAdmin().firstOrNull()
    if (admin != null) onAlreadyLoggedIn() else onNotLoggedIn()
}
