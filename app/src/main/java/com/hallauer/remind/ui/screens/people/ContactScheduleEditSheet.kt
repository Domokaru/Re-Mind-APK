package com.hallauer.remind.ui.screens.people

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.core.util.PhoneNumberNormalizer
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.RecurrenceType
import com.hallauer.remind.data.local.entity.TimeMode
import com.hallauer.remind.ui.theme.toColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScheduleEditSheet(
    schedule: ContactScheduleEntity? = null,
    onDismiss: () -> Unit,
    onSaveSchedule: (ContactScheduleEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var displayName by remember { mutableStateOf(schedule?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf(schedule?.phoneNumber ?: "") }
    var contactId by remember { mutableLongStateOf(schedule?.contactId ?: 0L) }
    var lookupKey by remember { mutableStateOf(schedule?.contactLookupKey ?: "") }
    var selectedBucket by remember { mutableStateOf(schedule?.bucket ?: GroupBucket.FRIENDS) }
    var recurrenceType by remember { mutableStateOf(schedule?.recurrenceType ?: RecurrenceType.WEEKLY) }
    var intervalN by remember { mutableIntStateOf(schedule?.intervalN ?: 2) }
    var selectedDaysCsv by remember { mutableStateOf(schedule?.daysOfWeekCsv ?: "1,4") } // Mon, Thu
    var dayOfMonth by remember { mutableIntStateOf(schedule?.dayOfMonth ?: 1) }
    var monthOfYear by remember { mutableIntStateOf(schedule?.monthOfYear ?: 1) }
    var timeMode by remember { mutableStateOf(schedule?.timeMode ?: TimeMode.RANDOM_WINDOW_1HR) }
    var targetHour by remember { mutableIntStateOf(schedule?.targetHour ?: 14) } // 2:00 PM default
    var targetMinute by remember { mutableIntStateOf(schedule?.targetMinute ?: 15) }

    // Direct Android Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { phoneUri ->
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
                )
                context.contentResolver.query(phoneUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                        val lookupIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)

                        if (nameIdx >= 0) {
                            cursor.getString(nameIdx)?.let { displayName = it }
                        }
                        if (numIdx >= 0) {
                            cursor.getString(numIdx)?.let { phoneNumber = it }
                        }
                        if (idIdx >= 0) {
                            contactId = cursor.getLong(idIdx)
                        }
                        if (lookupIdx >= 0) {
                            cursor.getString(lookupIdx)?.let { lookupKey = it }
                        }
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF111827)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = if (schedule == null) "Add Connection Schedule" else "Edit Schedule",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pick Contact Button (directly invokes Android Contacts)
            Button(
                onClick = {
                    val pickIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    contactPickerLauncher.launch(pickIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                    contentColor = Color(0xFF38BDF8)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = "Pick Contact",
                    tint = Color(0xFF38BDF8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pick Contact",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Name & Phone
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Contact Name", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (Mobile Default)", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Group Bucket Chips
            Text(
                text = "Group Bucket (Strictly 1)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GroupBucket.values().forEach { bucket ->
                    val isSelected = selectedBucket == bucket
                    val color = bucket.toColor(isDarkTheme = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF1E293B))
                            .clickable { selectedBucket = bucket }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bucket.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) color else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recurrence Type
            Text(
                text = "Recurrence Cadence",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RecurrenceType.values().forEach { type ->
                    val isSelected = recurrenceType == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { recurrenceType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { recurrenceType = type },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (type) {
                                RecurrenceType.DAILY -> "Every Day"
                                RecurrenceType.EVERY_N_DAYS -> "Every $intervalN Days"
                                RecurrenceType.WEEKLY -> "Weekly on Chosen Days"
                                RecurrenceType.EVERY_N_WEEKS -> "Every $intervalN Weeks"
                                RecurrenceType.MONTHLY_DAY -> "Monthly on Day $dayOfMonth"
                                RecurrenceType.YEARLY_DATE -> "Yearly on Date"
                            },
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Time Mode Selector (Exact Time vs 1-Hour Random Window)
            Text(
                text = "Dispatch Timing Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = timeMode == TimeMode.EXACT_TIME,
                    onClick = { timeMode = TimeMode.EXACT_TIME },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exact Time (e.g. 2:15 PM)", color = Color.White, fontSize = 13.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = timeMode == TimeMode.RANDOM_WINDOW_1HR,
                    onClick = { timeMode = TimeMode.RANDOM_WINDOW_1HR },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("1-Hour Random Window (e.g. 2:00 - 2:59 PM)", color = Color.White, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Cancel (Circle X) and Save (Save Icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        tint = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                val isSaveEnabled = displayName.isNotBlank() && phoneNumber.isNotBlank()
                IconButton(
                    onClick = {
                        if (isSaveEnabled) {
                            val finalLookup = if (lookupKey.isNotBlank()) lookupKey else "manual_${System.currentTimeMillis()}"
                            val finalId = if (contactId != 0L) contactId else System.currentTimeMillis()
                            val entity = ContactScheduleEntity(
                                id = schedule?.id ?: 0,
                                contactLookupKey = finalLookup,
                                contactId = finalId,
                                displayName = displayName.trim(),
                                phoneNumber = phoneNumber.trim(),
                                normalizedNumber = PhoneNumberNormalizer.normalize(phoneNumber),
                                bucket = selectedBucket,
                                recurrenceType = recurrenceType,
                                intervalN = intervalN,
                                daysOfWeekCsv = selectedDaysCsv,
                                dayOfMonth = dayOfMonth,
                                monthOfYear = monthOfYear,
                                timeMode = timeMode,
                                targetHour = targetHour.coerceIn(7, 22),
                                targetMinute = targetMinute.coerceIn(0, 59),
                                isActive = true
                            )
                            onSaveSchedule(entity)
                            onDismiss()
                        }
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSaveEnabled) Color(0xFF38BDF8) else Color(0xFF334155))
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Schedule",
                        tint = if (isSaveEnabled) Color(0xFF0F172A) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
