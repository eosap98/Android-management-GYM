package com.gymku.app.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.ShiftInfo
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AdminMenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    authViewModel: com.gymku.app.viewmodel.AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSchedule: () -> Unit = {}
) {
    val vm: AdminMenuViewModel = viewModel()
    val staffList by vm.staffList.collectAsState()
    val shiftList by vm.shiftList.collectAsState()

    var showStaffDialog by remember { mutableStateOf(false) }
    var showShiftDialog by remember { mutableStateOf(false) }
    var showAdminPassDialog by remember { mutableStateOf(false) }
    var selectedStaff by remember { mutableStateOf<Admin?>(null) }
    var selectedShift by remember { mutableStateOf<ShiftInfo?>(null) }
    val currentAdmin by authViewModel.currentAdmin.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Admin", fontWeight = FontWeight.SemiBold) },
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
                .padding(16.dp)
        ) {
            // Static Action Buttons
            if (currentAdmin?.role == "admin") {
                Button(
                    onClick = { showAdminPassDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ganti Password Admin", fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = onNavigateToSchedule,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoMain)
                ) {
                    Icon(Icons.Outlined.Event, null, tint = White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Atur Jadwal Shift Staff 1 Bulan", fontWeight = FontWeight.SemiBold)
                }
            }

            // Independent Scrollable Lists
            Column(modifier = Modifier.weight(1f)) {
                // Jadwal Shift Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jadwal Shift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { selectedShift = null; showShiftDialog = true }) {
                        Text("Tambah Shift", color = IndigoMain)
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (shiftList.isEmpty()) {
                        item {
                            Text("Belum ada jadwal shift.", color = Slate500, modifier = Modifier.padding(bottom = 16.dp))
                        }
                    } else {
                        items(shiftList) { shift ->
                            ShiftCard(shift = shift, onEdit = { selectedShift = shift; showShiftDialog = true }, onDelete = { vm.deleteShift(shift.id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(16.dp))

                // Pegawai (Staff) Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pegawai (Staff)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { selectedStaff = null; showStaffDialog = true }) {
                        Text("Tambah Staff", color = EmeraldGreen)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (staffList.isEmpty()) {
                        item {
                            Text("Belum ada pegawai terdaftar.", color = Slate500)
                        }
                    } else {
                        items(staffList) { staff ->
                            StaffCard(staff = staff, onEdit = { selectedStaff = staff; showStaffDialog = true }, onDelete = { vm.deleteStaff(staff.id) })
                        }
                    }
                }
            }
        }
    }

    if (showShiftDialog) {
        ShiftDialog(
            shift = selectedShift,
            onDismiss = { showShiftDialog = false },
            onSave = {
                vm.saveShift(it)
                showShiftDialog = false
            }
        )
    }

    if (showStaffDialog) {
        StaffDialog(
            staff = selectedStaff,
            onDismiss = { showStaffDialog = false },
            onSave = {
                vm.addStaff(it)
                showStaffDialog = false
            }
        )
    }

    if (showAdminPassDialog && currentAdmin != null) {
        AdminPassDialog(
            admin = currentAdmin!!,
            onDismiss = { showAdminPassDialog = false },
            onSave = { newPass ->
                vm.updateAdminPassword(currentAdmin!!, newPass)
                showAdminPassDialog = false
            }
        )
    }
}

@Composable
private fun ShiftCard(shift: ShiftInfo, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(shift.name, fontWeight = FontWeight.Bold)
                Text("${String.format("%02d:00", shift.startHour)} - ${String.format("%02d:00", shift.endHour)}", color = Slate500, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Edit", tint = IndigoMain) }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Delete", tint = RoseRed) }
            }
        }
    }
}

@Composable
private fun StaffCard(staff: Admin, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(staff.name, fontWeight = FontWeight.Bold)
                Text("Username: ${staff.username}", color = Slate500, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Edit", tint = EmeraldGreen) }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Delete", tint = RoseRed) }
            }
        }
    }
}

@Composable
private fun ShiftDialog(shift: ShiftInfo?, onDismiss: () -> Unit, onSave: (ShiftInfo) -> Unit) {
    var name by remember { mutableStateOf(shift?.name ?: "") }
    var startHour by remember { mutableStateOf(shift?.startHour?.toString() ?: "0") }
    var endHour by remember { mutableStateOf(shift?.endHour?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (shift == null) "Tambah Shift" else "Edit Shift") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Shift") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = startHour, onValueChange = { startHour = it }, label = { Text("Jam Mulai (0-23)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = endHour, onValueChange = { endHour = it }, label = { Text("Jam Selesai (0-23)") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sHour = startHour.toIntOrNull() ?: 0
                    val eHour = endHour.toIntOrNull() ?: 0
                    onSave(ShiftInfo(id = shift?.id ?: "", name = name, startHour = sHour, endHour = eHour))
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaffDialog(staff: Admin?, onDismiss: () -> Unit, onSave: (Admin) -> Unit) {
    var name by remember { mutableStateOf(staff?.name ?: "") }
    var username by remember { mutableStateOf(staff?.username ?: "") }
    var password by remember { mutableStateOf(staff?.password ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (staff == null) "Tambah Pegawai" else "Edit Pegawai") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(Admin(id = staff?.id ?: "", name = name, username = username, password = password, role = "staff", assignedShift = ""))
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AdminPassDialog(admin: Admin, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var password by remember { mutableStateOf(admin.password) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Password Admin", color = Slate900) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password Baru") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (password.isNotBlank()) onSave(password) }
            ) { Text("Simpan", color = IndigoMain) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Slate500) } },
        containerColor = White
    )
}
