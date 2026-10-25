package com.hallauer.remind.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.data.local.datastore.ThemeMode

@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    isGlobalPauseEnabled: Boolean,
    hasSmsPermission: Boolean = false,
    onThemeChanged: (ThemeMode) -> Unit,
    onGlobalPauseToggled: (Boolean) -> Unit,
    onSendTestSms: (String) -> Unit = {},
    onRequestSmsPermission: () -> Unit = {},
    onExportJsonClicked: () -> Unit = {},
    onImportJsonClicked: () -> Unit = {},
    onSyncFirebaseNow: () -> Unit = {},
    onOpenAndroidSettings: () -> Unit = {}
) {
    var testPhoneNumber by remember { mutableStateOf("") }
    var testSmsStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Settings & Diagnostics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Appearance Theme
        SectionTitle(title = "Appearance")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { mode ->
                    val isSelected = currentTheme == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF08DAC9) else Color(0xFF1E293B))
                            .clickable { onThemeChanged(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mode) {
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.SYSTEM -> "System"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF04201D) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Automation Controls
        SectionTitle(title = "Automation Controls")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vacation Mode",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Temporarily pause all automated dispatches",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isGlobalPauseEnabled,
                        onCheckedChange = onGlobalPauseToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF04201D),
                            checkedTrackColor = Color(0xFF08DAC9)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SimCard,
                        contentDescription = null,
                        tint = Color(0xFF08DAC9),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Primary SIM Subscription",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "Default hardware voice/SMS SIM (auto-detected)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Android Privacy & SMS Operating Modes
        SectionTitle(title = "Android Privacy & Dispatch Modes")
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (hasSmsPermission) Color(0xFF111827) else Color(0xFF172033)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (hasSmsPermission) Color(0xFF065F46) else Color(0xFF0284C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasSmsPermission) "Direct Silent SMS Active" else "Zero-Permission Mode Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (hasSmsPermission) {
                    Text(
                        text = "Your device has granted hardware SMS permissions. Re:MIND sends scheduled check-in texts automatically through your carrier SIM in the background.",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 17.sp
                    )
                } else {
                    Text(
                        text = "Modern Android protects privacy by blocking direct SMS permissions on sideloaded apps. Re:MIND runs reliably without needing any SMS permissions at all!",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Zero-Permission Workflow: When your scheduled check-in time arrives, you receive a high-priority notification. Tapping it opens Google Messages or Samsung Messages with the recipient and phrase pre-filled ready to send with 1 tap.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Want Silent Background SMS? (Optional on Android 13+):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF08DAC9)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Tap \"Open App Info\" below.\n2. Tap the ⋮ (three dots) in the top-right corner.\n3. Tap \"Allow restricted settings\" and confirm your PIN.\n4. Go into \"Permissions\" > \"SMS\" > \"Allow\".",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRequestSmsPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08DAC9)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF04201D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request SMS", color = Color(0xFF04201D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onOpenAndroidSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Open App Info", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: Carrier SMS / Pre-fill Diagnostic
        SectionTitle(title = "Messaging Diagnostic")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (hasSmsPermission) "Test Silent SMS Dispatch" else "Test Pre-filled Send",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (hasSmsPermission)
                        "Send a direct test SMS to confirm hardware carrier dispatch on this device."
                    else
                        "Test opening your messaging app with pre-filled text (works with zero permissions).",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = testPhoneNumber,
                    onValueChange = { testPhoneNumber = it },
                    label = { Text("Phone Number", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("+1 555-123-4567", color = Color(0xFF64748B)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF08DAC9),
                        unfocusedBorderColor = Color(0xFF334155),
                        cursorColor = Color(0xFF08DAC9)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (testPhoneNumber.isNotBlank()) {
                            testSmsStatus = if (hasSmsPermission) "Dispatching test SMS..." else "Opening messaging app..."
                            onSendTestSms(testPhoneNumber)
                        }
                    },
                    enabled = testPhoneNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF08DAC9),
                        contentColor = Color(0xFF04201D),
                        disabledContainerColor = Color(0xFF1E293B),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasSmsPermission) "Send Test Carrier SMS" else "Test Pre-fill in Messages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (testSmsStatus != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = testSmsStatus!!,
                        fontSize = 11.sp,
                        color = Color(0xFF08DAC9),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 5: Data & Cloud Backup
        SectionTitle(title = "Data & Backup")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Weekly Cloud Backup (Firestore)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Syncs schedules and phrase pools weekly with cloud storage.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Button(
                    onClick = onSyncFirebaseNow,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF08DAC9), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync to Cloud Now", color = Color.White, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Local JSON Backup",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Export or restore database backups via device storage.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportJsonClicked,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF08DAC9), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON", color = Color.White, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onImportJsonClicked,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFF08DAC9), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import JSON", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 6: System Permissions & Integrity
        SectionTitle(title = "System Permissions & Health")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HealthStatusRow(
                    title = "SMS Dispatch",
                    isGranted = hasSmsPermission,
                    note = if (hasSmsPermission) "Silent SIM carrier text dispatch" else "Zero-Permission pre-fill mode active"
                )
                HealthStatusRow(
                    title = "Google Contacts",
                    isGranted = true,
                    note = "Recipient phone verification"
                )
                HealthStatusRow(
                    title = "Exact Alarms",
                    isGranted = true,
                    note = "Scheduled dispatch timing"
                )
                HealthStatusRow(
                    title = "Notifications",
                    isGranted = true,
                    note = "Actionable check-in reminders & status"
                )

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onOpenAndroidSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage App Permissions", color = Color(0xFF08DAC9), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF08DAC9),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun HealthStatusRow(title: String, isGranted: Boolean, note: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = if (isGranted) Color(0xFF34D399) else Color(0xFF38BDF8),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = note, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}
