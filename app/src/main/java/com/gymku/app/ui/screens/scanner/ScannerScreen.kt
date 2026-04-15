package com.gymku.app.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.gymku.app.data.model.Member
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AuthViewModel
import com.gymku.app.viewmodel.ScannerState
import com.gymku.app.viewmodel.ScannerViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRenew: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: ScannerViewModel = viewModel()
    val scannerState by vm.state.collectAsState()
    val admin by authViewModel.currentAdmin.collectAsState()
    val shift by authViewModel.currentShift.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var lockerInput by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val showBottomSheet = scannerState is ScannerState.MemberFound
            || scannerState is ScannerState.NotFound
            || scannerState is ScannerState.CheckInSuccess

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera preview
        if (hasCameraPermission) {
            CameraPreview(
                onQrCodeScanned = { code -> vm.onQrCodeScanned(code) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Izin kamera diperlukan", color = White, textAlign = TextAlign.Center)
            }
        }

        // Scan overlay
        ScanOverlay()

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(44.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(12.dp))
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = White) }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Scan QR Member", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        // Loading indicator
        if (scannerState is ScannerState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = White)
            }
        }
    }

    // Bottom Sheet modal — outside the Box to avoid nesting issues
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { vm.resetScanner() },
            containerColor = White
        ) {
            when (val s = scannerState) {
                is ScannerState.MemberFound -> {
                    MemberFoundSheet(
                        member = s.member,
                        lockerInput = lockerInput,
                        onLockerChange = { lockerInput = it },
                        onConfirmCheckIn = {
                            vm.confirmCheckIn(
                                member = s.member,
                                adminId = admin?.id ?: "",
                                adminName = admin?.name ?: "",
                                shift = shift.name,
                                lockerNumber = lockerInput
                            )
                        },
                        onRenew = {
                            vm.resetScanner()
                            onNavigateToRenew(s.member.id)
                        },
                        onDismiss = { vm.resetScanner() }
                    )
                }
                is ScannerState.NotFound -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.SearchOff, null, tint = RoseRed, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Member Tidak Ditemukan", style = MaterialTheme.typography.headlineSmall, color = Slate900, fontWeight = FontWeight.Bold)
                        Text("QR: ${s.qrCode}", style = MaterialTheme.typography.bodySmall, color = Slate500, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
                        Button(
                            onClick = { vm.resetScanner() },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) { Text("Scan Ulang") }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is ScannerState.CheckInSuccess -> {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1200)
                        vm.resetScanner()
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Check-in Berhasil! ✓", style = MaterialTheme.typography.headlineSmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MemberFoundSheet(
    member: Member,
    lockerInput: String,
    onLockerChange: (String) -> Unit,
    onConfirmCheckIn: () -> Unit,
    onRenew: () -> Unit,
    onDismiss: () -> Unit
) {
    val isActive = member.isCurrentlyActive()
    val days     = member.daysRemaining()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp)
                    .background(if (isActive) EmeraldLight else RoseLight, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(member.getInitials(), color = if (isActive) EmeraldGreen else RoseRed, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(member.name, style = MaterialTheme.typography.headlineSmall, color = Slate900, fontWeight = FontWeight.Bold)
                Text("ID: ${member.id}", style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(20.dp), color = if (isActive) EmeraldLight else RoseLight) {
                Text(
                    if (isActive) "Aktif" else "Expired",
                    color = if (isActive) EmeraldGreen else RoseRed,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Slate200)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Slate100, RoundedCornerShape(12.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip("Sisa Hari", "$days hari", if (days > 7) EmeraldGreen else RoseRed)
            InfoChip("Expired", member.expireDate, Slate500)
            InfoChip("Gender", member.gender, Slate500)
        }
        if (isActive) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lockerInput, onValueChange = onLockerChange,
                label = { Text("No. Loker (opsional)") },
                leadingIcon = { Icon(Icons.Outlined.LockOpen, null, tint = Slate400) },
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoMain, unfocusedBorderColor = Slate200)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onConfirmCheckIn,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Konfirmasi Masuk", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRenew,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoseRed)
            ) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Perpanjang Paket", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Batal", color = Slate500)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
        Text(value, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ScanOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        val cornerLength = 32.dp
        val cornerThickness = 4.dp
        Box(modifier = Modifier.align(Alignment.Center).size(220.dp)) {
            Box(Modifier.size(cornerLength, cornerThickness).background(White).align(Alignment.TopStart))
            Box(Modifier.size(cornerThickness, cornerLength).background(White).align(Alignment.TopStart))
            Box(Modifier.size(cornerLength, cornerThickness).background(White).align(Alignment.TopEnd))
            Box(Modifier.size(cornerThickness, cornerLength).background(White).align(Alignment.TopEnd))
            Box(Modifier.size(cornerLength, cornerThickness).background(White).align(Alignment.BottomStart))
            Box(Modifier.size(cornerThickness, cornerLength).background(White).align(Alignment.BottomStart))
            Box(Modifier.size(cornerLength, cornerThickness).background(White).align(Alignment.BottomEnd))
            Box(Modifier.size(cornerThickness, cornerLength).background(White).align(Alignment.BottomEnd))
        }
        Text(
            "Arahkan QR code member ke dalam bingkai",
            color = White.copy(0.7f),
            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(top = 240.dp).padding(horizontal = 48.dp)
        )
    }
}

@Composable
private fun CameraPreview(onQrCodeScanned: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: Executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor, QrCodeAnalyzer(barcodeScanner, onQrCodeScanned))
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                } catch (e: Exception) { /* ignore */ }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}
