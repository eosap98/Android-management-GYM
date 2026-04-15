package com.gymku.app.ui.screens.member

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.Member
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AuthViewModel
import com.gymku.app.viewmodel.MemberActionState
import com.gymku.app.viewmodel.MemberViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    memberId: String = "", // Optional for edit mode
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onMemberAdded: () -> Unit
) {
    val vm: MemberViewModel = viewModel()
    val actionState by vm.actionState.collectAsState()
    val admin by authViewModel.currentAdmin.collectAsState()
    val shift by authViewModel.currentShift.collectAsState()
    val nf = NumberFormat.getInstance(Locale("id", "ID"))

    var name          by remember { mutableStateOf("") }
    var phone         by remember { mutableStateOf("") }
    var gender        by remember { mutableStateOf("L") }
    var months        by remember { mutableStateOf(1) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    
    val isEditMode = memberId.isNotEmpty()
    val members by vm.members.collectAsState()

    LaunchedEffect(memberId, members) {
        if (isEditMode) {
            members.find { it.id == memberId }?.let { m ->
                name = m.name
                phone = m.phone
                gender = m.gender
            }
        }
    }

    val basePrice = Member.PRICE_NEW_MEMBER
    val extraMonths = if (months > 1) months - 1 else 0
    val totalPrice = basePrice + (extraMonths * Member.PRICE_RENEW_PER_MONTH)

    LaunchedEffect(actionState) {
        if (actionState is MemberActionState.Success) {
            onMemberAdded()
            vm.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Member" else "Daftar Member Baru", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Fixed price note
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = IndigoMain, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Member Baru: Rp 150.000", style = MaterialTheme.typography.bodySmall, color = IndigoMain, fontWeight = FontWeight.Bold)
                        Text("Biaya pendaftaran sudah termasuk paket bulan pertama.", style = MaterialTheme.typography.bodySmall, color = IndigoMain)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("Data Pribadi")
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                leadingIcon = { Icon(Icons.Outlined.Person, null, tint = Slate400) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("No. HP") },
                leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = Slate400) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Jenis Kelamin")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("L" to "Laki-laki", "P" to "Perempuan").forEach { (value, label) ->
                    FilterChip(
                        selected = gender == value,
                        onClick = { gender = value },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoMain,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            if (!isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel("Paket Awal (Bulan)")
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1, 3, 6).forEach { m ->
                        val selected = months == m
                        OutlinedButton(
                            onClick = { months = m },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) IndigoMain else White,
                                contentColor   = if (selected) White else Slate900
                            )
                        ) {
                            Text("$m Bulan", fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel("Metode Pembayaran")
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Cash", "QRIS").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoMain,
                                selectedLabelColor = White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price summary
            if (!isEditMode) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Pembayaran", style = MaterialTheme.typography.bodySmall, color = White.copy(0.6f))
                            Text(
                                "Rp ${nf.format(totalPrice)}",
                                style = MaterialTheme.typography.displayMedium,
                                color = White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pendaftaran Baru", style = MaterialTheme.typography.bodySmall, color = EmeraldGreen)
                            Text("${months} Bulan · ${paymentMethod}", style = MaterialTheme.typography.bodySmall, color = White.copy(0.5f))
                        }
                    }
                }
            }

            // Error
            AnimatedVisibility(visible = actionState is MemberActionState.Error) {
                Text(
                    text = (actionState as? MemberActionState.Error)?.message ?: "",
                    color = RoseRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isEditMode) {
                        members.find { it.id == memberId }?.let { m ->
                            vm.updateMember(m.copy(name = name, phone = phone, gender = gender))
                        }
                    } else {
                        vm.addMember(
                            name = name, phone = phone, gender = gender, months = months,
                            paymentMethod = paymentMethod,
                            adminId = admin?.id ?: "", adminName = admin?.name ?: "",
                            shift = shift.name
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank() && actionState !is MemberActionState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isEditMode) EmeraldGreen else IndigoMain)
            ) {
                if (actionState is MemberActionState.Loading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isEditMode) "Simpan Perubahan" else "Daftarkan Member", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = Slate700, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = IndigoMain,
    unfocusedBorderColor = Slate200,
    focusedLabelColor = IndigoMain
)
