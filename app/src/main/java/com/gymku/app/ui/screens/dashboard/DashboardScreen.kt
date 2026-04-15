package com.gymku.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.VisitorHistory
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.AuthViewModel
import com.gymku.app.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToAddMember: () -> Unit,
    onNavigateToAddVisitor: () -> Unit,
    onNavigateToAdminMenu: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val dashVm: DashboardViewModel = viewModel()
    val admin by authViewModel.currentAdmin.collectAsState()
    val shift by authViewModel.currentShift.collectAsState()
    val data  by dashVm.dashboardData.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GymKu", fontWeight = FontWeight.Bold, color = White) },
                actions = {
                    if (admin?.role == "admin") {
                        IconButton(onClick = onNavigateToAdminMenu) {
                            Icon(Icons.Outlined.AdminPanelSettings, "Admin", tint = White)
                        }
                    }
                    IconButton(onClick = { authViewModel.logout(context) { onLogout() } }) {
                        Icon(Icons.Outlined.Logout, "Logout", tint = White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Slate900)
            )
        },
        bottomBar = {
            BottomBar(
                onHome     = {},
                onMembers  = onNavigateToMembers,
                onScanner  = onNavigateToScanner,
                onVisitors = onNavigateToAddVisitor,
                onReport   = onNavigateToReport
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScanner,
                containerColor = Slate900,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp).offset(y = 50.dp)
            ) {
                Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header (Static)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Slate900)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 40.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Halo, ${admin?.name ?: "Admin"} 👋",
                                color = White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (shift.name == "Luar Jam") AmberYellow.copy(0.15f) else IndigoMain.copy(0.2f)
                            ) {
                                 Text(
                                    text = "⏰ Shift ${shift.name}",
                                    color = if (shift.name == "Luar Jam") AmberYellow else IndigoLight,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metric cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCardSmall(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.People,
                            label = "Kehadiran Hari Ini",
                            value = "${data.memberCheckIns + data.visitorCount}",
                            subtitle = "${data.memberCheckIns} member · ${data.visitorCount} tamu",
                            iconBg = IndigoMain.copy(0.2f),
                            iconTint = IndigoLight
                        )
                        MetricCardSmall(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.AttachMoney,
                            label = "Pendapatan Hari Ini",
                            value = formatRupiah(data.todayRevenue),
                            subtitle = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID")).format(Date()),
                            iconBg = EmeraldGreen.copy(0.2f),
                            iconTint = EmeraldGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions Title (Static)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Riwayat Pengunjung",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate900,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToReport) {
                    Text("Riwayat Lengkap", color = IndigoMain, style = MaterialTheme.typography.labelLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Riwayat Pengunjung List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (data.visitorHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada pengunjung hari ini",
                                color = Slate400,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(data.visitorHistory) { history ->
                        VisitorHistoryRow(history)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCardSmall(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, color = White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, color = White.copy(0.7f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
            Text(subtitle, color = White.copy(0.5f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate700,
            lineHeight = 14.sp,
            modifier = Modifier.wrapContentWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun VisitorHistoryRow(history: VisitorHistory) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(history.timestamp))

    val (label, color) = when (history.type) {
        "Member" -> Pair("Member", EmeraldGreen)
        else     -> Pair("Harian", IndigoMain)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (history.type == "Member") Icons.Outlined.Person else Icons.Outlined.PersonOutline,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = history.name.ifEmpty { "Tamu Harian" },
                style = MaterialTheme.typography.bodyLarge,
                color = Slate900,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Check-in $time",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(0.1f)
        ) {
            Text(
                text = label,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
    HorizontalDivider(color = Slate200, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
}

@Composable
private fun BottomBar(
    onHome: () -> Unit,
    onMembers: () -> Unit,
    onScanner: () -> Unit,
    onVisitors: () -> Unit,
    onReport: () -> Unit
) {
    BottomAppBar(
        containerColor = White,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                selected = true,
                onClick = onHome,
                icon = { Icon(Icons.Outlined.Home, null) },
                label = { Text("Home", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = IndigoMain, indicatorColor = IndigoLight)
            )
            NavigationBarItem(
                selected = false,
                onClick = onMembers,
                icon = { Icon(Icons.Outlined.People, null) },
                label = { Text("Member", fontSize = 10.sp) }
            )
            
            // Spacer for FAB
            Spacer(modifier = Modifier.width(48.dp))

            NavigationBarItem(
                selected = false,
                onClick = onVisitors,
                icon = { Icon(Icons.Outlined.PersonAdd, null) },
                label = { Text("Tamu", fontSize = 10.sp) }
            )
            NavigationBarItem(
                selected = false,
                onClick = onReport,
                icon = { Icon(Icons.Outlined.BarChart, null) },
                label = { Text("Laporan", fontSize = 10.sp) }
            )
        }
    }
}

private fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getInstance(Locale("id", "ID"))
    return "Rp ${nf.format(amount)}"
}
