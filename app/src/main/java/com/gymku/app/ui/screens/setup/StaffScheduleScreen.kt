package com.gymku.app.ui.screens.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.Admin
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.StaffScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScheduleScreen(
    onNavigateBack: () -> Unit
) {
    val vm: StaffScheduleViewModel = viewModel()
    val monthYearTitle by vm.monthYearTitle.collectAsState()
    val calendarGrid by vm.calendarGrid.collectAsState()
    val staffList by vm.staffList.collectAsState()
    val shiftList by vm.shiftList.collectAsState()
    val schedules by vm.schedules.collectAsState()

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    val dayHeaders = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Shift 1 Bulan", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Month Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.prevMonth() }) { Icon(Icons.Outlined.ChevronLeft, "Prev Month", tint = IndigoMain) }
                Text(monthYearTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                IconButton(onClick = { vm.nextMonth() }) { Icon(Icons.Outlined.ChevronRight, "Next Month", tint = IndigoMain) }
            }
            HorizontalDivider(color = Slate200)

            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                // Headers
                items(dayHeaders) { header ->
                    Text(
                        text = header,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = if (header == "Min") RoseRed else Slate500,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Days
                items(calendarGrid) { day ->
                    if (!day.isCurrentMonth) {
                        Box(Modifier.aspectRatio(1f)) // Empty block
                    } else {
                        val daySchedules = schedules.filter { it.date == day.dateString }
                        val isRed = day.isSunday || day.holidayName != null
                        val isToday = day.dateString == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("id", "ID")).format(java.util.Date())
                        val blockColor = if (isRed) RoseRed.copy(alpha = 0.05f) else White

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clickable { 
                                    selectedDate = day.dateString
                                    searchQuery = ""
                                    showSheet = true 
                                }
                                .background(blockColor, RoundedCornerShape(8.dp))
                                .then(
                                    if (isToday) Modifier.border(1.5.dp, EmeraldGreen, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.displayDay,
                                    fontSize = 15.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isRed) RoseRed else Slate900
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))

                                // Holiday indicator
                                if (day.holidayName != null) {
                                    Text(
                                        text = day.holidayName!!,
                                        fontSize = 8.sp,
                                        color = RoseRed,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Assignment indicator
                                if (daySchedules.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                                        daySchedules.take(3).forEach { sc ->
                                            val color = if(sc.shiftName == "Pagi") EmeraldGreen else if(sc.shiftName == "Siang") AmberYellow else IndigoMain
                                            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                                        }
                                        if (daySchedules.size > 3) {
                                            Text("+", fontSize = 6.sp, color = Slate500, lineHeight = 6.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet && selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            windowInsets = WindowInsets.navigationBars
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Atur Jadwal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900)
                        Text(selectedDate!!, style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                    
                    // Clear all button
                    IconButton(
                        onClick = {
                            staffList.forEach { s -> vm.saveSchedule(selectedDate!!, s.id, "") }
                        }
                    ) {
                        Icon(Icons.Outlined.DeleteForever, "Clear Day", tint = RoseRed)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama staff...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(20.dp), tint = Slate400) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Slate100,
                        focusedBorderColor = IndigoMain,
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = Slate50
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Staff List
                val filteredStaff = if (searchQuery.isEmpty()) staffList else staffList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStaff) { staff ->
                        StaffScheduleRow(
                            staff = staff,
                            currentShift = schedules.find { it.date == selectedDate && it.adminId == staff.id }?.shiftName ?: "",
                            availableShifts = shiftList.map { it.name },
                            onShiftSelected = { shift ->
                                vm.saveSchedule(selectedDate!!, staff.id, shift)
                            }
                        )
                    }
                    
                    if (filteredStaff.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Staff tidak ditemukan", color = Slate400, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
                
                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Selesai", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StaffScheduleRow(
    staff: Admin, 
    currentShift: String, 
    availableShifts: List<String>, 
    onShiftSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = true },
        shape = RoundedCornerShape(12.dp),
        color = White,
        border = BorderStroke(1.dp, Slate100),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(IndigoLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, null, tint = IndigoMain, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(staff.name, fontWeight = FontWeight.SemiBold, color = Slate900, fontSize = 15.sp)
                Text(
                    text = if (currentShift.isEmpty()) "Belum ada shift" else "Shift: $currentShift",
                    color = if (currentShift.isEmpty()) Slate400 else EmeraldGreen,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Minimal Dropdown Indicator
            Box {
                Text(
                    text = "Ganti",
                    color = IndigoMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .background(IndigoLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { expanded = true }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Hapus Jadwal (Bebas)") },
                        onClick = { onShiftSelected(""); expanded = false },
                        leadingIcon = { Icon(Icons.Outlined.DeleteForever, null, tint = RoseRed, modifier = Modifier.size(18.dp)) }
                    )
                    HorizontalDivider(color = Slate50)
                    availableShifts.forEach { shift ->
                        DropdownMenuItem(
                            text = { Text(shift) },
                            onClick = { onShiftSelected(shift); expanded = false }
                        )
                    }
                }
            }
        }
    }
}
