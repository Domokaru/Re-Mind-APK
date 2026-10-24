package com.hallauer.remind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.core.util.LoveGuardrailValidator
import com.hallauer.remind.data.local.entity.MessageLogEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingMessageActionSheet(
    pendingLog: MessageLogEntity,
    onDismiss: () -> Unit,
    onSendNow: (MessageLogEntity) -> Unit,
    onRerollPhrase: (MessageLogEntity) -> Unit,
    onSaveEditedText: (MessageLogEntity, String) -> Unit,
    onSkipOccurrence: (MessageLogEntity) -> Unit,
    onRescheduleTime: (MessageLogEntity) -> Unit,
    onPauseContact: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEditingText by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(pendingLog.messageText) }
    var guardrailError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF111827)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header: Contact Info & Bucket
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pendingLog.contactDisplayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = pendingLog.contactPhoneNumber,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                BucketBadge(bucket = pendingLog.bucket)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Pre-rolled Message Preview or Inline Editor
            if (isEditingText) {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = {
                        editedText = it
                        val validation = LoveGuardrailValidator.validate(pendingLog.bucket, it)
                        guardrailError = if (validation is LoveGuardrailValidator.ValidationResult.Invalid) {
                            validation.errorMessage
                        } else null
                    },
                    label = { Text("Edit One-Off Message", color = Color(0xFF94A3B8)) },
                    isError = guardrailError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (guardrailError != null) {
                    Text(
                        text = guardrailError!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(
                        onClick = { isEditingText = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (guardrailError == null && editedText.isNotBlank()) {
                                onSaveEditedText(pendingLog, editedText)
                                isEditingText = false
                            }
                        },
                        enabled = guardrailError == null && editedText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08DAC9)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Text", color = Color(0xFF04201D), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                BoxContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "\"${pendingLog.messageText}\"",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6 Interactive Actions List
            ActionSheetItem(
                icon = Icons.Default.Send,
                title = "Send Now",
                subtitle = "Dispatch immediately and roll next occurrence",
                tint = Color(0xFF08DAC9),
                onClick = {
                    onSendNow(pendingLog)
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Casino,
                title = "Reroll Phrase",
                subtitle = "Pick a different phrase from ${pendingLog.bucket.displayName} pool",
                tint = Color(0xFFFBBF24),
                onClick = {
                    onRerollPhrase(pendingLog)
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Edit,
                title = "Edit Text (One-Off)",
                subtitle = "Customize message text for just this single send",
                tint = Color(0xFFA78BFA),
                onClick = { isEditingText = true }
            )

            ActionSheetItem(
                icon = Icons.Default.SkipNext,
                title = "Skip This Occurrence",
                subtitle = "Mark skipped and pre-roll the following date",
                tint = Color(0xFF94A3B8),
                onClick = {
                    onSkipOccurrence(pendingLog)
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Schedule,
                title = "Reschedule Time / Date",
                subtitle = "Shift target send time within 7:00 AM - 10:59 PM",
                tint = Color(0xFF34D399),
                onClick = {
                    onRescheduleTime(pendingLog)
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.PauseCircleOutline,
                title = "Pause Contact Schedule",
                subtitle = "Disable future automated check-ins for this person",
                tint = Color(0xFFF87171),
                onClick = {
                    onPauseContact(pendingLog.contactScheduleId)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun BoxContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

@Composable
private fun ActionSheetItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
