package com.hallauer.remind.ui.screens.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.RecurrenceType
import com.hallauer.remind.data.local.entity.TimeMode
import com.hallauer.remind.ui.components.BucketBadge

@Composable
fun PeopleScreen(
    schedules: List<ContactScheduleEntity>,
    onToggleActive: (Long, Boolean) -> Unit = { _, _ -> },
    onSaveSchedule: (ContactScheduleEntity) -> Unit = {},
    onDeleteSchedule: (ContactScheduleEntity) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf<String>("All") }
    var editingSchedule by remember { mutableStateOf<ContactScheduleEntity?>(null) }
    var scheduleToDelete by remember { mutableStateOf<ContactScheduleEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    val filterOptions = remember {
        listOf("All") + GroupBucket.values().map { it.displayName } + listOf("Paused")
    }

    val filteredList = schedules.filter { schedule ->
        when (selectedFilter) {
            "All" -> true
            "Paused" -> !schedule.isActive
            else -> schedule.bucket.displayName.equals(selectedFilter, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Schedule Cards
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Text(
                            text = "No contacts in this category.",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    items(filteredList, key = { it.id }) { schedule ->
                        ContactScheduleCard(
                            schedule = schedule,
                            onToggleActive = { onToggleActive(schedule.id, it) },
                            onEdit = { editingSchedule = schedule },
                            onDelete = { scheduleToDelete = schedule }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }

        // Add Person FAB
        FloatingActionButton(
            onClick = { isAddingNew = true },
            containerColor = Color(0xFF38BDF8),
            contentColor = Color(0xFF0F172A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact Schedule")
        }
    }

    if (isAddingNew) {
        ContactScheduleEditSheet(
            schedule = null,
            onDismiss = { isAddingNew = false },
            onSaveSchedule = onSaveSchedule
        )
    }

    editingSchedule?.let { schedule ->
        ContactScheduleEditSheet(
            schedule = schedule,
            onDismiss = { editingSchedule = null },
            onSaveSchedule = onSaveSchedule
        )
    }

    scheduleToDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = "Delete Connection?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${schedule.displayName}? All recurring schedules, locked-in upcoming messages, and activity history for this contact will be permanently deleted.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSchedule(schedule)
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Connection", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun ContactScheduleCard(
    schedule: ContactScheduleEntity,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = schedule.displayName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isExpanded) {
                        Text(
                            text = schedule.phoneNumber,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                BucketBadge(bucket = schedule.bucket)
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))

                // Recurrence Summary String
                val recurrenceSummary = formatRecurrenceSummary(schedule)
                Text(
                    text = recurrenceSummary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8)
                )

                // Warning if paused by monthly verify
                if (!schedule.pausedReason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = schedule.pausedReason,
                            fontSize = 11.sp,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Schedule", tint = Color(0xFF94A3B8))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Connection", tint = Color(0xFFF87171))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (schedule.isActive) "Active" else "Paused",
                            fontSize = 12.sp,
                            color = if (schedule.isActive) Color(0xFF34D399) else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = schedule.isActive,
                            onCheckedChange = onToggleActive,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0F172A),
                                checkedTrackColor = Color(0xFF38BDF8)
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatRecurrenceSummary(schedule: ContactScheduleEntity): String {
    val cadence = when (schedule.recurrenceType) {
        RecurrenceType.DAILY -> "Every Day"
        RecurrenceType.EVERY_N_DAYS -> "Every ${schedule.intervalN} days"
        RecurrenceType.WEEKLY -> "Weekly"
        RecurrenceType.EVERY_N_WEEKS -> "Every ${schedule.intervalN} weeks"
        RecurrenceType.MONTHLY_DAY -> "Monthly on day ${schedule.dayOfMonth}"
        RecurrenceType.YEARLY_DATE -> "Yearly"
    }

    val timeStr = when (schedule.timeMode) {
        TimeMode.EXACT_TIME -> {
            val h = if (schedule.targetHour % 12 == 0) 12 else schedule.targetHour % 12
            val amPm = if (schedule.targetHour >= 12) "PM" else "AM"
            String.format("%d:%02d %s", h, schedule.targetMinute, amPm)
        }
        TimeMode.RANDOM_WINDOW_1HR -> {
            val h = if (schedule.targetHour % 12 == 0) 12 else schedule.targetHour % 12
            val amPm = if (schedule.targetHour >= 12) "PM" else "AM"
            "$h:00 - $h:59 $amPm random window"
        }
    }

    return "$cadence • $timeStr"
}
