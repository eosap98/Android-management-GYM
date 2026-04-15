package com.gymku.app.ui.screens.member

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymku.app.data.model.Member
import com.gymku.app.ui.theme.*
import com.gymku.app.viewmodel.MemberViewModel

@Composable
fun MemberListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val vm: MemberViewModel = viewModel()
    val members by vm.filteredMembers.collectAsState()
    val query   by vm.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(White).padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = Slate900)
                    }
                    Text(
                        "Daftar Member",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::setSearch,
                    placeholder = { Text("Cari nama, ID, atau No.HP...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Slate400) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { vm.setSearch("") }) {
                                Icon(Icons.Outlined.Close, null, tint = Slate400)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoMain,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${members.size} member",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Slate900,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.PersonAdd, null, tint = White)
            }
        },
        containerColor = Slate50
    ) { padding ->
        if (members.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.PeopleOutline, null, tint = Slate200, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Belum ada member", color = Slate400, style = MaterialTheme.typography.bodyLarge)
                    Text("Tap + untuk mendaftarkan member baru", color = Slate400, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(members, key = { it.id }) { member ->
                    SwipeableMemberRow(
                        member = member,
                        onClick = { onNavigateToDetail(member.id) },
                        onSwipeToRenew = { onNavigateToDetail(member.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableMemberRow(
    member: Member,
    onClick: () -> Unit,
    onSwipeToRenew: () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onSwipeToRenew()
            }
            false // Don't actually dismiss
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val color by animateColorAsState(
                if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) EmeraldGreen else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(color).padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Refresh, null, tint = White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Perpanjang", color = White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        enableDismissFromEndToStart = false
    ) {
        MemberRow(member = member, onClick = onClick)
    }
}

@Composable
fun MemberRow(member: Member, onClick: () -> Unit) {
    val isActive = member.isCurrentlyActive()
    val days = member.daysRemaining()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with initials
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (isActive) IndigoLight else RoseLight,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.getInitials(),
                color = if (isActive) IndigoMain else RoseRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate900,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${member.id} · ${if (isActive) "Sisa $days hari" else "Expired"}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }
        // Status chip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isActive) EmeraldLight else RoseLight
        ) {
            Text(
                text = if (isActive) "Aktif" else "Expired",
                color = if (isActive) EmeraldGreen else RoseRed,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
    HorizontalDivider(color = Slate200, thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp, end = 24.dp))
}
