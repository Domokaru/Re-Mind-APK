package com.hallauer.remind.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.MessageStatus
import com.hallauer.remind.ui.components.BucketBadge
import com.hallauer.remind.ui.components.UpcomingMessageActionSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    upcomingMessages: List<MessageLogEntity>,
    recentHistory: List<MessageLogEntity>,
    hasMissingPermissions: Boolean = false,
    onRequestPermissions: () -> Unit = {},
    onSendNow: (MessageLogEntity) -> Unit = {},
    onRerollPhrase: (MessageLogEntity) -> Unit = {},
    onSaveEditedText: (MessageLogEntity, String) -> Unit = { _, _ -> },
    onSkipOccurrence: (MessageLogEntity) -> Unit = {},
    onRescheduleTime: (MessageLogEntity) -> Unit = {},
    onPauseContact: (Long) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: History
    var activeActionSheetLog by remember { mutableStateOf<MessageLogEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
    ) {
        // Section Tabs (Upcoming vs History)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0B0F19),
            contentColor = Color(0xFF38BDF8),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF38BDF8)
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Upcoming (${upcomingMessages.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Color.White else Color(0xFF94A3B8)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "History (${recentHistory.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) Color.White else Color(0xFF94A3B8)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feed list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedTab == 0) {
                if (upcomingMessages.isEmpty()) {
                    item {
                        EmptyStateCard(message = "No upcoming messages scheduled. Add contacts in the People tab to start automating connection.")
                    }
                } else {
                    items(upcomingMessages, key = { it.id }) { log ->
                        UpcomingMessageCard(
                            log = log,
                            onClick = { activeActionSheetLog = log }
                        )
                    }
                }
            } else {
                if (recentHistory.isEmpty()) {
                    item {
                        EmptyStateCard(message = "No sent messages or collision skips yet. When messages execute, they will appear here.")
                    }
                } else {
                    items(recentHistory, key = { it.id }) { log ->
                        HistoryMessageCard(log = log)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Modal action sheet
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

@Composable
fun UpcomingMessageCard(
    log: MessageLogEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar initials circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = log.contactDisplayName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.contactDisplayName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = formatDateTime(log.scheduledEpochMillis),
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                BucketBadge(bucket = log.bucket)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phrase text
            Text(
                text = "\"${log.messageText}\"",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFFE2E8F0)
            )

            if (log.isCustomEdited) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Customized for this send",
                    fontSize = 11.sp,
                    color = Color(0xFFA78BFA)
                )
            }
        }
    }
}

@Composable
fun HistoryMessageCard(log: MessageLogEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.contactDisplayName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    val execTime = log.executedEpochMillis ?: log.scheduledEpochMillis
                    Text(
                        text = formatDateTime(execTime),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                StatusPill(status = log.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${log.messageText}\"",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFFCBD5E1)
            )

            if (!log.statusNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.statusNote,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun StatusPill(status: MessageStatus) {
    val (bgColor, textColor, label) = when (status) {
        MessageStatus.SENT -> Triple(Color(0xFF065F46), Color(0xFF34D399), "SENT")
        MessageStatus.SKIPPED_COLLISION -> Triple(Color(0xFF78350F), Color(0xFFFBBF24), "SKIPPED: RECENT TEXT")
        MessageStatus.SKIPPED_MANUAL -> Triple(Color(0xFF1E293B), Color(0xFF94A3B8), "SKIPPED: MANUAL")
        MessageStatus.FAILED -> Triple(Color(0xFF7F1D1D), Color(0xFFF87171), "FAILED")
        MessageStatus.PENDING -> Triple(Color(0xFF0C4A6E), Color(0xFF38BDF8), "PENDING")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatDateTime(epochMillis: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
