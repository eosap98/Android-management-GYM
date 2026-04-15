package com.gymku.app.ui.screens.report

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.Transaction
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.ReportFilter
import com.gymku.app.viewmodel.ReportViewModel
import com.gymku.app.viewmodel.ReportData
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    authViewModel: com.gymku.app.viewmodel.AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val vm: ReportViewModel = viewModel()
    val filter by vm.filter.collectAsState()
    val data   by vm.reportData.collectAsState()
    val admin  by authViewModel.currentAdmin.collectAsState()
    val nf = NumberFormat.getInstance(Locale("id", "ID"))
    
    var showDeleteConfirm by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, null) }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        val label = when(filter.type) {
                            ReportFilter.FilterType.TODAY -> "Hari Ini"
                            ReportFilter.FilterType.THIS_WEEK -> "Minggu Ini"
                            ReportFilter.FilterType.THIS_MONTH -> "Bulan Ini"
                            ReportFilter.FilterType.CUSTOM -> "${filter.startDate} - ${filter.endDate} (${filter.shiftName})"
                        }
                        com.gymku.app.util.ReportExporter.exportToPdfAndShare(context, data, label)
                    }) {
                        Icon(Icons.Outlined.PictureAsPdf, "Export PDF", tint = IndigoMain)
                    }
                    IconButton(onClick = {
                        val reportText = buildWhatsAppReport(data, admin?.name ?: "-", filter.shiftName, nf)
                        shareToWhatsApp(context, reportText)
                    }) {
                        Icon(Icons.Outlined.Share, "Share WhatsApp", tint = EmeraldGreen)
                    }
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
        ) {
            // Static Summary Sections
            Column {
                // Filter tabs
                if (admin?.role == "staff") {
                    // Staff can only see today
                    Box(modifier = Modifier.fillMaxWidth().background(White).padding(16.dp)) {
                        Text("Laporan Hari Ini", fontWeight = FontWeight.Bold, color = Slate900)
                    }
                } else {
                    // Admin filters
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                ReportFilter.FilterType.TODAY to "Hari Ini",
                                ReportFilter.FilterType.THIS_WEEK to "Minggu",
                                ReportFilter.FilterType.THIS_MONTH to "Bulan Ini",
                                ReportFilter.FilterType.CUSTOM to "Custom"
                            ).forEach { (fType, label) ->
                                FilterChip(
                                    selected = filter.type == fType,
                                    onClick = { vm.setFilter(ReportFilter(type = fType)) },
                                    label = { Text(label, fontWeight = if (filter.type == fType) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Slate900,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                        }
                        
                        if (filter.type == ReportFilter.FilterType.CUSTOM) {
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = filter.startDate,
                                    onValueChange = { vm.setFilter(filter.copy(startDate = it)) },
                                    label = { Text("Dari Tgl", fontSize = 12.sp) }, 
                                    placeholder = { Text("yyyy-MM-dd", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = filter.endDate,
                                    onValueChange = { vm.setFilter(filter.copy(endDate = it)) },
                                    label = { Text("Sampai Tgl", fontSize = 12.sp) },
                                    placeholder = { Text("yyyy-MM-dd", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = filter.shiftName,
                                    onValueChange = { vm.setFilter(filter.copy(shiftName = it)) },
                                    label = { Text("Shift", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Slate200)

                Spacer(modifier = Modifier.height(16.dp))

                // Revenue summary card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("Total Pendapatan", style = MaterialTheme.typography.bodyMedium, color = White.copy(0.6f))
                        Text(
                            "Rp ${nf.format(data.totalRevenue)}",
                            style = MaterialTheme.typography.displayMedium,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RevenueChip(
                                modifier = Modifier.weight(1f),
                                label = "Cash",
                                amount = "Rp ${nf.format(data.totalCash)}",
                                color = EmeraldGreen,
                                bg = EmeraldGreen.copy(0.15f)
                            )
                            RevenueChip(
                                modifier = Modifier.weight(1f),
                                label = "QRIS",
                                amount = "Rp ${nf.format(data.totalQris)}",
                                color = IndigoLight,
                                bg = IndigoMain.copy(0.15f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activity breakdown
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(Modifier.weight(1f), "Member Baru", "${data.newMemberCount}", Icons.Outlined.PersonAdd, IndigoMain, IndigoLight)
                    StatCard(Modifier.weight(1f), "Perpanjangan", "${data.renewCount}", Icons.Outlined.Refresh, EmeraldGreen, EmeraldLight)
                    StatCard(Modifier.weight(1f), "Tamu Harian", "${data.visitorCount}", Icons.Outlined.PersonOutline, AmberYellow, AmberLight)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Transaction list header
                Text(
                    "Riwayat Transaksi (${data.transactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate900,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Scrollable Transaction List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (data.transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.ReceiptLong, null, tint = Slate200, modifier = Modifier.size(56.dp))
                                Text("Tidak ada transaksi", color = Slate400, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                } else {
                    items(data.transactions) { tx ->
                        TxRow(tx, nf, admin?.role == "admin") {
                            showDeleteConfirm = tx
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Hapus Transaksi") },
            text = { Text("Hapus transaksi ${showDeleteConfirm?.memberName ?: "Tamu"}?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteTransaction(showDeleteConfirm!!.id)
                    showDeleteConfirm = null
                }) { Text("Hapus", color = RoseRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Batal") }
            },
            containerColor = White
        )
    }
}

@Composable
private fun RevenueChip(modifier: Modifier, label: String, amount: String, color: Color, bg: Color) {
    Box(modifier = modifier.background(bg, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(amount, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color, bg: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(40.dp).background(bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Slate900, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TxRow(tx: Transaction, nf: NumberFormat, isAdmin: Boolean, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(tx.timestamp))

    val (label, color, icon) = when (tx.type) {
        Transaction.TYPE_NEW_MEMBER -> Triple("Member Baru", IndigoMain, Icons.Outlined.PersonAdd)
        Transaction.TYPE_RENEW      -> Triple("Perpanjangan", EmeraldGreen, Icons.Outlined.Refresh)
        Transaction.TYPE_VISITOR    -> Triple("Tamu Harian", AmberYellow, Icons.Outlined.PersonOutline)
        else -> Triple("Transaksi", Slate500, Icons.Outlined.Receipt)
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(White).padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(color.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(tx.memberName.ifEmpty { "Tamu" }, style = MaterialTheme.typography.bodyMedium, color = Slate900, fontWeight = FontWeight.Medium)
            Text("$label · ${tx.paymentMethod} · $time · ${tx.shift}", style = MaterialTheme.typography.bodySmall, color = Slate500)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text("Rp ${nf.format(tx.amount)}", style = MaterialTheme.typography.bodyMedium, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
            if (isAdmin) {
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = RoseRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
    HorizontalDivider(color = Slate200, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp, end = 24.dp))
}

private fun shareToWhatsApp(context: android.content.Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val genericIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        context.startActivity(android.content.Intent.createChooser(genericIntent, "Share Report"))
    }
}

private fun buildWhatsAppReport(data: ReportData, adminName: String, shift: String, nf: java.text.NumberFormat): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val dateStr = sdf.format(java.util.Date())
    
    val visitorCash = data.transactions.filter { tx: Transaction -> tx.type == Transaction.TYPE_VISITOR && tx.paymentMethod == "Cash" }.sumOf { tx: Transaction -> tx.amount }
    val visitorQris = data.transactions.filter { tx: Transaction -> tx.type == Transaction.TYPE_VISITOR && tx.paymentMethod == "QRIS" }.sumOf { tx: Transaction -> tx.amount }
    
    val newMemberCash = data.transactions.filter { tx: Transaction -> tx.type == Transaction.TYPE_NEW_MEMBER && tx.paymentMethod == "Cash" }.sumOf { tx: Transaction -> tx.amount }
    val newMemberQris = data.transactions.filter { tx: Transaction -> tx.type == Transaction.TYPE_NEW_MEMBER && tx.paymentMethod == "QRIS" }.sumOf { tx: Transaction -> tx.amount }
    
    val renewCash = data.transactions.filter { tx: Transaction -> tx.type == Transaction.TYPE_RENEW && tx.paymentMethod == "Cash" }.sumOf { tx: Transaction -> tx.amount }
    val renewQris = data.transactions.filter { tx: Transaction -> tx.type == Transaction.TYPE_RENEW && tx.paymentMethod == "QRIS" }.sumOf { tx: Transaction -> tx.amount }

    return """
        *Laporan Fitness*
        
        Petugas Jaga : $adminName
        Jaga : $shift
        Hari : $dateStr
        
        *Kunjungan* 
        Member Harian : ${data.visitorCount} orang
        by Cash : Rp. ${nf.format(visitorCash.toLong())}
        by Qris : Rp. ${nf.format(visitorQris.toLong())}
        
        Member Bulanan : ${data.checkInCount} orang
        Member Bulanan Baru : ${data.newMemberCount} org (Cash : Rp. ${nf.format(newMemberCash.toLong())}) (Qris : Rp. ${nf.format(newMemberQris.toLong())})
        Member Perpanjang : ${data.renewCount} org (Cash : Rp. ${nf.format(renewCash.toLong())}) (Qris : Rp. ${nf.format(renewQris.toLong())})
             
        Jumlah Kunjungan : ${data.visitorCount + data.checkInCount} org
        
        Jumlah Pendapatan Tunai : Rp. ${nf.format(data.totalCash.toLong())}
        Jumlah Bayar Qris : Rp. ${nf.format(data.totalQris.toLong())}
        *Total : Rp. ${nf.format(data.totalRevenue.toLong())}*
           
        Sewa Lapangan Badminton : Rp. 0
    """.trimIndent()
}

