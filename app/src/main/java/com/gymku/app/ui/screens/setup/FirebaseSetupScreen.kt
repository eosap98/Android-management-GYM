package com.gymku.app.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.FirebaseConfig
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.FirebaseSetupState
import com.gymku.app.viewmodel.FirebaseSetupViewModel

@Composable
fun FirebaseSetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val vm: FirebaseSetupViewModel = viewModel()
    val state by vm.state.collectAsState()

    var databaseUrl by remember { mutableStateOf("") }
    var projectId   by remember { mutableStateOf("") }
    var appId       by remember { mutableStateOf("") }
    var apiKey      by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is FirebaseSetupState.Success) onSetupComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(IndigoLight, RoundedCornerShape(20.dp))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Cloud, contentDescription = null, tint = IndigoMain, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Koneksi Firebase",
            style = MaterialTheme.typography.headlineLarge,
            color = Slate900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Masukkan konfigurasi Firebase Realtime Database Anda untuk menghubungkan aplikasi.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Form fields
        SetupTextField(
            value = databaseUrl,
            onValueChange = { databaseUrl = it },
            label = "Database URL",
            placeholder = "https://your-project-default-rtdb.firebaseio.com",
            icon = Icons.Outlined.Link
        )
        Spacer(modifier = Modifier.height(16.dp))
        SetupTextField(
            value = projectId,
            onValueChange = { projectId = it },
            label = "Project ID",
            placeholder = "your-project-id",
            icon = Icons.Outlined.Cloud
        )
        Spacer(modifier = Modifier.height(16.dp))
        SetupTextField(
            value = appId,
            onValueChange = { appId = it },
            label = "App ID",
            placeholder = "1:123456:android:abcdef",
            icon = Icons.Outlined.Key
        )
        Spacer(modifier = Modifier.height(16.dp))
        SetupTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = "API Key",
            placeholder = "AIzaSy...",
            icon = Icons.Outlined.Key
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Error display
        AnimatedVisibility(visible = state is FirebaseSetupState.Error) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RoseLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = (state as? FirebaseSetupState.Error)?.message ?: "",
                    color = RoseRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                vm.saveAndInit(
                    context = context,
                    config  = FirebaseConfig(
                        databaseUrl = databaseUrl, projectId = projectId,
                        appId = appId, apiKey = apiKey
                    )
                )
            },
            enabled = state !is FirebaseSetupState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoMain)
        ) {
            if (state is FirebaseSetupState.Loading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Simpan & Hubungkan", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Konfigurasi ini disimpan secara lokal di perangkat.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SetupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Slate400) },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IndigoMain,
            unfocusedBorderColor = Slate200,
            focusedLabelColor = IndigoMain
        )
    )
}
