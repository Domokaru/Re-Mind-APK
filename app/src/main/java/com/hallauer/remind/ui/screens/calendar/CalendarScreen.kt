package com.hallauer.remind.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.hallauer.remind.core.util.VirtualCalendarOccurrence
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.MessageStatus
import com.hallauer.remind.ui.components.BucketBadge
import com.hallauer.remind.ui.components.UpcomingMessageActionSheet
import com.hallauer.remind.ui.screens.dashboard.StatusPill
import com.hallauer.remind.ui.theme.toColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    pendingMessages: List<MessageLogEntity>,
    historyMessages: List<MessageLogEntity>,
    virtualProjections: List<VirtualCalendarOccurrence>,
    onSendNow: (MessageLogEntity) -> Unit = {},
    onRerollPhrase: (MessageLogEntity) -> Unit = {},
    onSaveEditedText: (MessageLogEntity, String) -> Unit = { _, _ -> },
    onSkipOccurrence: (MessageLogEntity) -> Unit = {},
    onRescheduleTime: (MessageLogEntity) -> Unit = {},
    onPauseContact: (Long) -> Unit = {}
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var activeActionSheetLog by remember { mutableStateOf<MessageLogEntity?>(null) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7 // Sunday = 0, Monday = 1

    // Filter events for selected date
    val selectedDateIso = selectedDate.toString()
    val dayPending = pendingMessages.filter { it.scheduledDateIso == selectedDateIso }
    val dayHistory = historyMessages.filter { it.scheduledDateIso == selectedDateIso }
    val dayProjections = virtualProjections.filter { it.projectedDate == selectedDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = Color.White)
                }
                TextButton(onClick = {
                    currentYearMonth = YearMonth.now()
                    selectedDate = LocalDate.now()
                }) {
                    Text("Today", color = Color(0xFF08DAC9), fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = Color.White)
                }
            }
        }

        // Days of week header (Sun, Mon, Tue...)
        Row(modifier = Modifier.fillMaxWidth()) {
            val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
            dayHeaders.forEach { header ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = header, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Month Calendar Grid (6 rows max)
        Column(modifier = Modifier.fillMaxWidth()) {
            var dayCounter = 1
            for (row in 0..5) {
                if (dayCounter > daysInMonth) break
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    for (col in 0..6) {
                        if (row == 0 && col < firstDayOfWeek || dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).height(44.dp))
                        } else {
                            val thisDay = dayCounter
                            val thisDate = currentYearMonth.atDay(thisDay)
                            val isSelected = thisDate == selectedDate
                            val isToday = thisDate == LocalDate.now()

                            // Gather bucket dots for this day
                            val thisDateIso = thisDate.toString()
                            val pendingOnDay = pendingMessages.filter { it.scheduledDateIso == thisDateIso }
                            val historyOnDay = historyMessages.filter { it.scheduledDateIso == thisDateIso }
                            val projectedOnDay = virtualProjections.filter { it.projectedDate == thisDate }
                            val totalCount = pendingOnDay.size + historyOnDay.size + projectedOnDay.size

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF1E293B)
                                            isToday -> Color(0xFF0F172A)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                        color = if (isSelected) Color(0xFF08DAC9) else if (isToday) Color(0xFF08DAC9).copy(alpha = 0.5f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedDate = thisDate },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$thisDay",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF08DAC9) else Color.White
                                    )

                                    // Dot indicators for buckets
                                    if (totalCount > 0) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            val distinctBuckets = (pendingOnDay.map { it.bucket } +
                                                    historyOnDay.map { it.bucket } +
                                                    projectedOnDay.map { it.bucket }).distinct().take(3)
                                            distinctBuckets.forEach { b ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(b.toColor(isDarkTheme = true))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Agenda Header
        Text(
            text = "Agenda for ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Day Agenda List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (dayPending.isEmpty() && dayHistory.isEmpty() && dayProjections.isEmpty()) {
                item {
                    Text(
                        text = "No scheduled messages or past history on this date.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            // Locked-in PENDING occurrence
            items(dayPending, key = { "pending_${it.id}" }) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeActionSheetLog = log }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.contactDisplayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            BucketBadge(bucket = log.bucket)
                        }
                        Text(
                            text = "\"${log.messageText}\"",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "Locked-in Next-1 • Tap for actions",
                            fontSize = 11.sp,
                            color = Color(0xFF08DAC9)
                        )
                    }
                }
            }

            // Virtual Projections (Subsequent occurrences after Next-1)
            items(dayProjections, key = { "proj_${it.contactScheduleId}_${it.projectedDate}" }) { proj ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF111827).copy(alpha = 0.6f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = proj.displayName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            BucketBadge(bucket = proj.bucket)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Projected: ${proj.timeSummary}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = proj.placeholderNote,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Historical events on this date
            items(dayHistory, key = { "hist_${it.id}" }) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.contactDisplayName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(status = log.status)
                        }
                        Text(
                            text = "\"${log.messageText}\"",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    activeActionSheetLog?.let { log ->
        UpcomingMessageActionSheet(
            pendingLog = log,
            onDismiss = { activeActionSheetLog = null },
            onSendNow = onSendNow,
            onRerollPhrase = onRerollPhrase,
            onSaveEditedText = onSaveEditedText,
            onSkipOccurrence = onSkipOccurrence,
            onRescheduleTime = onRescheduleTime,
            onPauseContact = onPauseContact
        )
    }
}
