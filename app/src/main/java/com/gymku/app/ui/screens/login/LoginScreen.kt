package com.gymku.app.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymku.app.data.model.ShiftInfo
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AuthState
import com.gymku.app.viewmodel.AuthViewModel
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    var username     by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val shift by authViewModel.currentShift.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn, is AuthState.AlreadyLoggedIn -> onLoginSuccess()
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Top curved header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Slate900)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(IndigoMain, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("GymKu", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                // Shift badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (shift.name == "Luar Jam") AmberLight else IndigoLight,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Shift ${shift.name} · ${shift.startHour}:00–${shift.endHour}:00",
                        color = if (shift.name == "Luar Jam") AmberYellow else IndigoMain,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Login card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    "Masuk",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Slate900,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Login dengan akun staf Anda",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; authViewModel.resetError() },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = Slate400) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoMain, unfocusedBorderColor = Slate200,
                        focusedLabelColor = IndigoMain
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; authViewModel.resetError() },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Slate400) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                null, tint = Slate400
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoMain, unfocusedBorderColor = Slate200,
                        focusedLabelColor = IndigoMain
                    )
                )

                AnimatedVisibility(visible = authState is AuthState.Error) {
                    Text(
                        text = (authState as? AuthState.Error)?.message ?: "",
                        color = RoseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { authViewModel.login(context, username, password) },
                    enabled = authState !is AuthState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoMain)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Masuk", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Default: admin / admin123",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Credit variables
        val uriHandler = LocalUriHandler.current
        val instagramGradient = remember {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4DB0FF), // Blueish
                    Color(0xFF9E77F1), // Purple
                    Color(0xFFD62976), // Pink
                    Color(0xFFFA7E1E), // Orange
                    Color(0xFFF7EB3B)  // Yellow
                )
            )
        }

        // Footer Section (Warning + Credit)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Development Warning Box
            Surface(
                color = AmberYellow.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberYellow.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person, 
                        contentDescription = null,
                        tint = AmberYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Aplikasi ini dalam tahap pengembangan, jika ada error, bug atau alur sistem yang tidak sesuai tolong beri tahu saya.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate700,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Credit
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "develop by ",
                    color = Slate500,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            uriHandler.openUri("https://instagram.com/eosnada1702")
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Eos Ageng",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            brush = instagramGradient
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = "Instagram",
                        tint = Color(0xFFD62976),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
