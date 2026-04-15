package com.gymku.app.ui.screens.member

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.gymku.app.data.model.Member
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AuthViewModel
import com.gymku.app.viewmodel.MemberActionState
import com.gymku.app.viewmodel.MemberViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val vm: MemberViewModel = viewModel()
    val members by vm.members.collectAsState()
    val actionState by vm.actionState.collectAsState()
    val admin by authViewModel.currentAdmin.collectAsState()
    val shift by authViewModel.currentShift.collectAsState()

    val member = members.firstOrNull { it.id == memberId }

    var showRenewSheet by remember { mutableStateOf(false) }
    var selectedMonths by remember { mutableStateOf(1) }
    var paymentMethod by remember { mutableStateOf("Cash") }

    var showCheckInDialog by remember { mutableStateOf(false) }
    var checkInMessage by remember { mutableStateOf("") }

    LaunchedEffect(actionState) {
        if (actionState is MemberActionState.Success) {
            val msg = (actionState as MemberActionState.Success).message
            if (msg.contains("Check-in")) {
                checkInMessage = msg
                showCheckInDialog = true
            } else if (msg.contains("berhasil dihapus")) {
                onNavigateBack()
            } else {
                showRenewSheet = false
            }
            vm.resetActionState()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Member", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, null)
                    }
                },
                actions = {
                    val context = LocalContext.current
                    if (admin?.role == "admin" && member != null) {
                        IconButton(onClick = { onNavigateToEdit(member.id) }) {
                            Icon(Icons.Outlined.Edit, "Edit Member", tint = EmeraldGreen)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Outlined.Delete, "Hapus Member", tint = RoseRed)
                        }
                        IconButton(onClick = { com.gymku.app.util.CardExporter.exportCardAndShare(context, member) }) {
                            Icon(Icons.Outlined.PictureAsPdf, "Bagikan Kartu PDF", tint = IndigoMain)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        if (member == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoMain)
            }
            return@Scaffold
        }

        val isActive = member.isCurrentlyActive()
        val days = member.daysRemaining()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero profile card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(if (isActive) IndigoLight else RoseLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(member.getInitials(), color = if (isActive) IndigoMain else RoseRed, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(member.name, style = MaterialTheme.typography.headlineMedium, color = Slate900, fontWeight = FontWeight.Bold)
                    Text(member.id, style = MaterialTheme.typography.bodySmall, color = Slate500, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isActive) EmeraldLight else RoseLight
                    ) {
                        Text(
                            if (isActive) "✓ Aktif · Sisa $days hari" else "✗ Expired",
                            color = if (isActive) EmeraldGreen else RoseRed,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // QR Code
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("QR Code Member", style = MaterialTheme.typography.titleMedium, color = Slate900, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    val qrBitmap = remember(member.qrCode) { generateQrBitmap(member.qrCode) }
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(180.dp)
                        )
                    }
                    Text(member.qrCode, style = MaterialTheme.typography.bodySmall, color = Slate400, modifier = Modifier.padding(top = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info details
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Informasi Member", style = MaterialTheme.typography.titleMedium, color = Slate900, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoRow(Icons.Outlined.Phone, "No. HP", member.phone.ifEmpty { "-" })
                    InfoRow(Icons.Outlined.Person, "Jenis Kelamin", if (member.gender == "L") "Laki-laki" else "Perempuan")
                    InfoRow(Icons.Outlined.CalendarToday, "Tanggal Bergabung", member.joinDate)
                    InfoRow(Icons.Outlined.EventBusy, "Expired", member.expireDate)
                    if (member.lastCheckIn.isNotEmpty()) {
                        InfoRow(Icons.Outlined.Login, "Check-in Terakhir", member.lastCheckIn)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check In Manual button
            if (isActive) {
                Button(
                    onClick = {
                        vm.manualCheckIn(
                            member = member,
                            adminId = admin?.id ?: "",
                            adminName = admin?.name ?: "",
                            shift = shift.name
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen) // Different color
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check In Manual", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Renew button
            Button(
                onClick = { showRenewSheet = true },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoMain)
            ) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Perpanjang Keanggotaan", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Renew bottom sheet
    if (showRenewSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRenewSheet = false },
            containerColor = White
        ) {
            RenewSheet(
                member = member!!,
                selectedMonths = selectedMonths,
                onMonthsSelected = { selectedMonths = it },
                paymentMethod = paymentMethod,
                onPaymentMethodChange = { paymentMethod = it },
                isLoading = actionState is MemberActionState.Loading,
                onConfirm = {
                    vm.renewMember(
                        memberId = member.id,
                        months = selectedMonths,
                        paymentMethod = paymentMethod,
                        adminId = admin?.id ?: "",
                        adminName = admin?.name ?: "",
                        shift = shift.name
                    )
                },
                onDismiss = { showRenewSheet = false }
            )
        }
    }

    if (showCheckInDialog) {
        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = { Text("Info Check-In", fontWeight = FontWeight.SemiBold, color = Slate900) },
            text = { Text(checkInMessage, color = Slate500) },
            confirmButton = {
                TextButton(onClick = { showCheckInDialog = false }) {
                    Text("Tutup", color = IndigoMain)
                }
            },
            containerColor = White
        )
    }

    if (showDeleteDialog && member != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Member", fontWeight = FontWeight.SemiBold, color = Slate900) },
            text = { Text("Apakah Anda yakin ingin menghapus ${member.name}? Data yang dihapus tidak bisa dikembalikan.", color = Slate500) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteMember(member)
                    showDeleteDialog = false
                }) {
                    Text("Hapus Sekarang", color = RoseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Slate500)
                }
            },
            containerColor = White
        )
    }
}

@Composable
private fun RenewSheet(
    member: Member,
    selectedMonths: Int,
    onMonthsSelected: (Int) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val nf = NumberFormat.getInstance(Locale("id", "ID"))
    val price = selectedMonths * Member.PRICE_RENEW_PER_MONTH

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Perpanjang Keanggotaan", style = MaterialTheme.typography.headlineSmall, color = Slate900, fontWeight = FontWeight.Bold)
        Text(member.name, style = MaterialTheme.typography.bodyMedium, color = Slate500, modifier = Modifier.padding(top = 2.dp, bottom = 20.dp))

        Text("Durasi Paket", style = MaterialTheme.typography.titleSmall, color = Slate700, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(1, 3, 6).forEach { m ->
                val selected = selectedMonths == m
                OutlinedButton(
                    onClick = { onMonthsSelected(m) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) IndigoMain else White,
                        contentColor   = if (selected) White else Slate900
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = if (selected) 0.dp else 1.dp
                    )
                ) {
                    Text("$m Bulan", fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Metode Bayar", style = MaterialTheme.typography.titleSmall, color = Slate700, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Cash", "QRIS").forEach { method ->
                val selected = paymentMethod == method
                FilterChip(
                    selected = selected,
                    onClick = { onPaymentMethodChange(method) },
                    label = { Text(method, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoMain,
                        selectedLabelColor = White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndigoLight, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Biaya", style = MaterialTheme.typography.bodyMedium, color = IndigoMain, fontWeight = FontWeight.SemiBold)
            Text("Rp ${nf.format(price)}", style = MaterialTheme.typography.headlineSmall, color = IndigoMain, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onConfirm,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoMain)
        ) {
            if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text("Konfirmasi Perpanjangan", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Batal", color = Slate500)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Slate400, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Slate500, modifier = Modifier.width(120.dp).padding(start = 10.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Slate900, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
}

private fun generateQrBitmap(text: String): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) for (y in 0 until 512)
            bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        bmp
    } catch (e: Exception) { null }
}
