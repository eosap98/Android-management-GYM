package com.gymku.app.ui.screens.visitor

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.Transaction
import com.gymku.app.data.model.Visitor
import com.gymku.app.data.repository.TransactionRepository
import com.gymku.app.data.repository.VisitorRepository
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Inline ViewModel for simplicity
class AddVisitorViewModel : ViewModel() {
    private val visitorRepo = VisitorRepository()
    private val txRepo = TransactionRepository()

    private val _state = MutableStateFlow<VisitorState>(VisitorState.Idle)
    val state: StateFlow<VisitorState> = _state.asStateFlow()

    sealed class VisitorState {
        object Idle : VisitorState()
        object Loading : VisitorState()
        object Success : VisitorState()
        data class Error(val msg: String) : VisitorState()
    }

    fun addVisitor(name: String, amount: Long, paymentMethod: String, adminId: String, adminName: String, shift: String) {
        viewModelScope.launch {
            _state.value = VisitorState.Loading
            try {
                val visitor = Visitor(name = name.ifBlank { "Tamu" }, amount = amount, paymentMethod = paymentMethod, adminId = adminId, adminName = adminName, shift = shift)
                visitorRepo.addVisitor(visitor)
                txRepo.addTransaction(Transaction(
                    type = Transaction.TYPE_VISITOR, memberName = name.ifBlank { "Tamu" },
                    amount = amount, paymentMethod = paymentMethod,
                    adminId = adminId, adminName = adminName, shift = shift
                ))
                _state.value = VisitorState.Success
            } catch (e: Exception) {
                _state.value = VisitorState.Error(e.message ?: "Gagal menyimpan")
            }
        }
    }
    fun reset() { _state.value = VisitorState.Idle }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitorScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onVisitorAdded: () -> Unit
) {
    val vm: AddVisitorViewModel = viewModel()
    val state by vm.state.collectAsState()
    val admin by authViewModel.currentAdmin.collectAsState()
    val shift by authViewModel.currentShift.collectAsState()
    val nf = NumberFormat.getInstance(Locale("id", "ID"))

    var name          by remember { mutableStateOf("") }
    var amountStr     by remember { mutableStateOf("10000") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    LaunchedEffect(state) {
        if (state is AddVisitorViewModel.VisitorState.Success) {
            onVisitorAdded()
            vm.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Pengunjung Harian", fontWeight = FontWeight.SemiBold) },
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
            // Header info
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PersonOutline, null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Pengunjung Harian", style = MaterialTheme.typography.bodySmall, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                        Text("Shift ${shift.name} — ${admin?.name ?: "Admin"}", style = MaterialTheme.typography.bodySmall, color = EmeraldGreen.copy(0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Pengunjung (opsional)") },
                placeholder = { Text("Kosongkan jika tidak ingin dicatat") },
                leadingIcon = { Icon(Icons.Outlined.Person, null, tint = Slate400) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoMain, unfocusedBorderColor = Slate200)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                label = { Text("Biaya Pengunjung Harian (Rp)") },
                leadingIcon = { Icon(Icons.Outlined.AttachMoney, null, tint = Slate400) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoMain, unfocusedBorderColor = Slate200)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall, color = Slate700, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Cash" to Icons.Outlined.Money, "QRIS" to Icons.Outlined.QrCode).forEach { (method, icon) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (paymentMethod == method) IndigoMain else White
                        ),
                        onClick = { paymentMethod = method }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, tint = if (paymentMethod == method) White else Slate400, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(method, color = if (paymentMethod == method) White else Slate900, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary
            val amount = amountStr.toLongOrNull() ?: 0L
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
                    Text("Total", style = MaterialTheme.typography.bodyMedium, color = White.copy(0.6f))
                    Text("Rp ${nf.format(amount)}", style = MaterialTheme.typography.headlineMedium, color = White, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = state is AddVisitorViewModel.VisitorState.Error) {
                Text((state as? AddVisitorViewModel.VisitorState.Error)?.msg ?: "", color = RoseRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    vm.addVisitor(
                        name = name, amount = amountStr.toLongOrNull() ?: 10_000L,
                        paymentMethod = paymentMethod,
                        adminId = admin?.id ?: "", adminName = admin?.name ?: "",
                        shift = shift.name
                    )
                },
                enabled = state !is AddVisitorViewModel.VisitorState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                if (state is AddVisitorViewModel.VisitorState.Loading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
