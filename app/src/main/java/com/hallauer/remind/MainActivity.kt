package com.hallauer.remind

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.core.util.BackupJsonSerializer
import com.hallauer.remind.core.util.ContactScheduleBackupDto
import com.hallauer.remind.core.util.DatabaseBackupDto
import com.hallauer.remind.core.util.MessageLogBackupDto
import com.hallauer.remind.core.util.PhraseBackupDto
import com.hallauer.remind.core.util.RecurrenceCalculator
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.local.datastore.SettingsDataStore
import com.hallauer.remind.data.local.datastore.ThemeMode
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.MessageStatus
import com.hallauer.remind.data.local.entity.PhraseEntity
import com.hallauer.remind.data.local.entity.RecurrenceType
import com.hallauer.remind.data.local.entity.TimeMode
import com.hallauer.remind.data.provider.SmsDispatcher
import com.hallauer.remind.data.remote.FirebaseBackupManager
import com.hallauer.remind.data.repository.RemindRepository
import com.hallauer.remind.ui.components.GlobalPauseBanner
import com.hallauer.remind.ui.navigation.BottomNavItems
import com.hallauer.remind.ui.navigation.Screen
import com.hallauer.remind.ui.screens.calendar.CalendarScreen
import com.hallauer.remind.ui.screens.dashboard.DashboardScreen
import com.hallauer.remind.ui.screens.people.PeopleScreen
import com.hallauer.remind.ui.screens.phrases.PhrasesScreen
import com.hallauer.remind.ui.screens.settings.SettingsScreen
import com.hallauer.remind.ui.theme.ReMINDTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

class MainActivity : ComponentActivity() {

    private var crashDiagnosticMessage by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process-wide crash safety logging
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("RemindFatal", "FATAL EXCEPTION on thread ${thread.name}", throwable)
            runOnUiThread {
                val stack = throwable.stackTrace.take(6).joinToString("\n") {
                    "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
                }
                crashDiagnosticMessage = "${throwable::class.java.simpleName}: ${throwable.message}\n$stack"
            }
        }

        // Main thread looper protection: intercepts unhandled exceptions to prevent instant process death
        Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (t: Throwable) {
                    android.util.Log.e("RemindCrashShield", "Intercepted UI exception: ${t.message}", t)
                    runOnUiThread {
                        val stack = t.stackTrace.take(6).joinToString("\n") {
                            "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
                        }
                        crashDiagnosticMessage = "${t::class.java.simpleName}: ${t.message}\n$stack"
                    }
                }
            }
        }

        setContent {
            val diagnosticError = crashDiagnosticMessage
            if (diagnosticError != null) {
                ReMINDTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Re:MIND Diagnostic Recovery",
                                    color = Color(0xFF08DAC9),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = diagnosticError,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            crashDiagnosticMessage = null
                                            recreate()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08DAC9))
                                    ) {
                                        Text("Retry", color = Color(0xFF04201D), fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            runCatching { applicationContext.deleteDatabase("remind_database.db") }
                                            crashDiagnosticMessage = null
                                            recreate()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                                    ) {
                                        Text("Reset DB & Restart", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                return@setContent
            }

            val scope = rememberCoroutineScope()
            val database = remember { RemindDatabase.getInstance(applicationContext) }
            val repository = remember {
                RemindRepository(
                    context = applicationContext,
                    contactScheduleDao = database.contactScheduleDao(),
                    phraseDao = database.phraseDao(),
                    messageLogDao = database.messageLogDao()
                )
            }
            val settingsDataStore = remember { SettingsDataStore(applicationContext) }

            // Runtime permissions requester for Contacts and Notifications (benign on cold-start)
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { _ -> /* Permissions results handled seamlessly */ }

            // Dedicated SMS permission requester for manual enablement in Settings
            val smsPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    Toast.makeText(applicationContext, "SMS permission granted! Direct carrier dispatch enabled.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "SMS permission not granted. Operating in Zero-Permission mode.", Toast.LENGTH_LONG).show()
                }
            }

            LaunchedEffect(Unit) {
                runCatching {
                    val permissions = mutableListOf(
                        Manifest.permission.READ_CONTACTS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                }
            }

            val themeMode by settingsDataStore.themeModeFlow.collectAsState(initial = ThemeMode.DARK)
            val isGlobalPause by settingsDataStore.isGlobalPauseEnabledFlow.collectAsState(initial = false)

            val schedules by repository.allSchedulesFlow.collectAsState(initial = emptyList())
            val pendingLogs by repository.pendingLogsFlow.collectAsState(initial = emptyList())
            val historyLogs by repository.executedHistoryFlow.collectAsState(initial = emptyList())
            val allPhrases by repository.allPhrasesFlow.collectAsState(initial = emptyList())

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri: Uri? ->
                if (uri != null) {
                    scope.launch(Dispatchers.IO) {
                        runCatching {
                            val allSchedules = database.contactScheduleDao().getAllSchedules()
                            val allPhrasesList = database.phraseDao().getAllPhrases()
                            val pendingLogsList = database.messageLogDao().getPendingLogs()

                            val dto = DatabaseBackupDto(
                                exportTimestampEpochMillis = System.currentTimeMillis(),
                                schedules = allSchedules.map { s ->
                                    ContactScheduleBackupDto(
                                        id = s.id,
                                        contactLookupKey = s.contactLookupKey,
                                        contactId = s.contactId,
                                        displayName = s.displayName,
                                        phoneNumber = s.phoneNumber,
                                        normalizedNumber = s.normalizedNumber,
                                        bucket = s.bucket.name,
                                        recurrenceType = s.recurrenceType.name,
                                        intervalN = s.intervalN,
                                        daysOfWeekCsv = s.daysOfWeekCsv,
                                        dayOfMonth = s.dayOfMonth,
                                        monthOfYear = s.monthOfYear,
                                        timeMode = s.timeMode.name,
                                        targetHour = s.targetHour,
                                        targetMinute = s.targetMinute,
                                        isActive = s.isActive,
                                        lastSentPhraseText = s.lastSentPhraseText
                                    )
                                },
                                phrases = allPhrasesList.map { p ->
                                    PhraseBackupDto(
                                        id = p.id,
                                        bucket = p.bucket.name,
                                        text = p.text,
                                        isSeed = p.isSeed
                                    )
                                },
                                logs = pendingLogsList.map { l ->
                                    MessageLogBackupDto(
                                        id = l.id,
                                        contactScheduleId = l.contactScheduleId,
                                        contactDisplayName = l.contactDisplayName,
                                        contactPhoneNumber = l.contactPhoneNumber,
                                        bucket = l.bucket.name,
                                        messageText = l.messageText,
                                        scheduledDateIso = l.scheduledDateIso,
                                        scheduledEpochMillis = l.scheduledEpochMillis,
                                        executedEpochMillis = l.executedEpochMillis,
                                        status = l.status.name,
                                        statusNote = l.statusNote
                                    )
                                }
                            )

                            val jsonStr = BackupJsonSerializer.serialize(dto)
                            contentResolver.openOutputStream(uri)?.use { stream ->
                                stream.write(jsonStr.toByteArray(Charsets.UTF_8))
                            }
                        }.onSuccess {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Re:MIND backup exported successfully!", Toast.LENGTH_LONG).show()
                            }
                        }.onFailure { err ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Export failed: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            val importLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                if (uri != null) {
                    scope.launch(Dispatchers.IO) {
                        runCatching {
                            val rawJson = contentResolver.openInputStream(uri)?.use { stream ->
                                stream.bufferedReader().readText()
                            } ?: throw IllegalStateException("Cannot read selected file.")

                            val backupDto = BackupJsonSerializer.deserialize(rawJson).getOrThrow()

                            backupDto.phrases.forEach { pDto ->
                                val bucket = runCatching { GroupBucket.valueOf(pDto.bucket) }.getOrDefault(GroupBucket.OTHERS)
                                database.phraseDao().insertPhrase(
                                    PhraseEntity(
                                        id = if (pDto.id > 0) pDto.id else 0L,
                                        bucket = bucket,
                                        text = pDto.text,
                                        isSeed = pDto.isSeed
                                    )
                                )
                            }

                            backupDto.schedules.forEach { sDto ->
                                val bucket = runCatching { GroupBucket.valueOf(sDto.bucket) }.getOrDefault(GroupBucket.OTHERS)
                                val recType = runCatching { RecurrenceType.valueOf(sDto.recurrenceType) }.getOrDefault(RecurrenceType.DAILY)
                                val tMode = runCatching { TimeMode.valueOf(sDto.timeMode) }.getOrDefault(TimeMode.EXACT_TIME)

                                val entity = ContactScheduleEntity(
                                    id = if (sDto.id > 0) sDto.id else 0L,
                                    contactLookupKey = sDto.contactLookupKey,
                                    contactId = sDto.contactId,
                                    displayName = sDto.displayName,
                                    phoneNumber = sDto.phoneNumber,
                                    normalizedNumber = sDto.normalizedNumber,
                                    bucket = bucket,
                                    recurrenceType = recType,
                                    intervalN = sDto.intervalN,
                                    daysOfWeekCsv = sDto.daysOfWeekCsv,
                                    dayOfMonth = sDto.dayOfMonth,
                                    monthOfYear = sDto.monthOfYear,
                                    timeMode = tMode,
                                    targetHour = sDto.targetHour,
                                    targetMinute = sDto.targetMinute,
                                    isActive = sDto.isActive,
                                    lastSentPhraseText = sDto.lastSentPhraseText
                                )
                                val insertedId = database.contactScheduleDao().insertSchedule(entity)
                                val saved = entity.copy(id = if (entity.id == 0L) insertedId else entity.id)
                                repository.preRollNextOccurrence(saved)
                            }
                            backupDto
                        }.onSuccess { dto ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "Imported ${dto.schedules.size} contacts and ${dto.phrases.size} phrases!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }.onFailure { err ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Import failed: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            // Calculate virtual calendar projections for current visible month
            val thisMonth = remember { YearMonth.now() }
            val virtualProjections = remember(schedules, pendingLogs) {
                val monthStart = thisMonth.atDay(1)
                val monthEnd = thisMonth.atEndOfMonth()
                schedules.filter { it.isActive }.flatMap { sched ->
                    val pendingDate = pendingLogs.find { it.contactScheduleId == sched.id }?.let {
                        runCatching { LocalDate.parse(it.scheduledDateIso) }.getOrNull()
                    } ?: LocalDate.now()
                    RecurrenceCalculator.projectFutureOccurrences(sched, pendingDate, monthStart, monthEnd)
                }
            }

            val handleSendNow: (MessageLogEntity) -> Unit = { log ->
                scope.launch {
                    val dispatcher = SmsDispatcher(applicationContext)
                    val sched = schedules.find { it.id == log.contactScheduleId }

                    if (dispatcher.hasSendSmsPermission()) {
                        val res = dispatcher.dispatchSms(
                            phoneNumber = log.contactPhoneNumber,
                            messageText = log.messageText,
                            logId = log.id
                        )
                        withContext(Dispatchers.Main) {
                            if (res.isSuccess) {
                                Toast.makeText(applicationContext, "Dispatched SMS to ${log.contactDisplayName}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(applicationContext, "SMS failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Zero-Permission Mode: launch pre-filled messaging app
                        runCatching {
                            val intent = dispatcher.createSendIntent(log.contactPhoneNumber, log.messageText)
                            startActivity(intent)
                        }.onFailure { err ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Unable to launch messaging app: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    // Update database log as sent and pre-roll next occurrence
                    val sentLog = log.copy(
                        status = MessageStatus.SENT,
                        executedEpochMillis = System.currentTimeMillis(),
                        statusNote = if (dispatcher.hasSendSmsPermission()) "Sent via device SMS" else "Opened in Messages app (Zero-permission mode)"
                    )
                    database.messageLogDao().updateLog(sentLog)

                    if (sched != null) {
                        val updatedSched = sched.copy(
                            lastSentPhraseText = log.messageText,
                            lastDispatchedDateIso = LocalDate.now().toString()
                        )
                        database.contactScheduleDao().updateSchedule(updatedSched)
                        repository.preRollNextOccurrence(updatedSched)
                    }
                }
            }

            ReMINDTheme(darkTheme = themeMode != ThemeMode.LIGHT) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Re:MIND",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    val activeCount = schedules.count { it.isActive }
                                    Text(
                                        text = if (isGlobalPause) "PAUSED — Vacation Mode" else "$activeCount Active Schedules",
                                        fontSize = 11.sp,
                                        color = if (isGlobalPause) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { currentScreen = Screen.Settings }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF0B0F19)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF111827),
                            tonalElevation = 0.dp
                        ) {
                            BottomNavItems.forEach { item ->
                                val selected = currentScreen == item
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentScreen = item },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF04201D),
                                        selectedTextColor = Color(0xFF08DAC9),
                                        indicatorColor = Color(0xFF08DAC9),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isGlobalPause) {
                            GlobalPauseBanner(
                                onResumeClicked = {
                                    scope.launch { settingsDataStore.setGlobalPause(false) }
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentScreen) {
                                Screen.Dashboard -> DashboardScreen(
                                    upcomingMessages = pendingLogs,
                                    recentHistory = historyLogs,
                                    onRequestPermissions = { openAppInfoSettings() },
                                    onSendNow = handleSendNow,
                                    onRerollPhrase = { log ->
                                        scope.launch { repository.rerollPendingMessage(log.id) }
                                    },
                                    onSaveEditedText = { log, text ->
                                        scope.launch { repository.updatePendingTextMessage(log.id, text) }
                                    },
                                    onSkipOccurrence = { log ->
                                        scope.launch { repository.skipOccurrence(log.id) }
                                    },
                                    onRescheduleTime = { log ->
                                        // Shifts scheduled time
                                    },
                                    onPauseContact = { schedId ->
                                        scope.launch { repository.setContactActiveState(schedId, false) }
                                    }
                                )

                                Screen.Calendar -> CalendarScreen(
                                    pendingMessages = pendingLogs,
                                    historyMessages = historyLogs,
                                    virtualProjections = virtualProjections,
                                    onSendNow = handleSendNow,
                                    onRerollPhrase = { log ->
                                        scope.launch { repository.rerollPendingMessage(log.id) }
                                    },
                                    onSaveEditedText = { log, text ->
                                        scope.launch { repository.updatePendingTextMessage(log.id, text) }
                                    },
                                    onSkipOccurrence = { log ->
                                        scope.launch { repository.skipOccurrence(log.id) }
                                    },
                                    onPauseContact = { schedId ->
                                        scope.launch { repository.setContactActiveState(schedId, false) }
                                    }
                                )

                                Screen.People -> PeopleScreen(
                                    schedules = schedules,
                                    onToggleActive = { schedId, active ->
                                        scope.launch { repository.setContactActiveState(schedId, active) }
                                    },
                                    onSaveSchedule = { sched ->
                                        scope.launch {
                                             val insertedId = database.contactScheduleDao().insertSchedule(sched)
                                            val fullSched = sched.copy(id = if (sched.id == 0L) insertedId else sched.id)
                                            repository.preRollNextOccurrence(fullSched)
                                        }
                                    },
                                    onDeleteSchedule = { sched ->
                                        scope.launch {
                                            repository.cancelAlarm(sched.id)
                                            database.contactScheduleDao().deleteSchedule(sched)
                                        }
                                    }
                                )

                                Screen.Phrases -> PhrasesScreen(
                                    phrases = allPhrases,
                                    onSavePhrase = { phrase ->
                                        scope.launch {
                                            if (phrase.id == 0L) {
                                                repository.createPhrase(phrase.bucket, phrase.text)
                                            } else {
                                                repository.updatePhraseAndCascade(phrase)
                                            }
                                        }
                                    },
                                    onDeletePhrase = { phrase ->
                                        scope.launch { repository.deletePhraseAndCascade(phrase) }
                                    }
                                )

                                Screen.Settings -> SettingsScreen(
                                    currentTheme = themeMode,
                                    isGlobalPauseEnabled = isGlobalPause,
                                    hasSmsPermission = SmsDispatcher(applicationContext).hasSendSmsPermission(),
                                    onThemeChanged = { mode ->
                                        scope.launch { settingsDataStore.setThemeMode(mode) }
                                    },
                                    onGlobalPauseToggled = { paused ->
                                        scope.launch { settingsDataStore.setGlobalPause(paused) }
                                    },
                                    onRequestSmsPermission = {
                                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    },
                                    onSendTestSms = { phoneNumber ->
                                        val dispatcher = SmsDispatcher(applicationContext)
                                        if (dispatcher.hasSendSmsPermission()) {
                                            scope.launch(Dispatchers.IO) {
                                                val res = dispatcher.dispatchSms(
                                                    phoneNumber = phoneNumber,
                                                    messageText = "Re:MIND Carrier Test: Hardware SMS radio is operational.",
                                                    logId = 999999L
                                                )
                                                withContext(Dispatchers.Main) {
                                                    if (res.isSuccess) {
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "Carrier test SMS dispatched successfully to $phoneNumber!",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        val err = res.exceptionOrNull()?.message ?: "Radio error"
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "Carrier test SMS failed: $err",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            runCatching {
                                                val intent = dispatcher.createSendIntent(
                                                    phoneNumber = phoneNumber,
                                                    messageText = "Re:MIND Test: Zero-permission SMS composer is operational."
                                                )
                                                startActivity(intent)
                                                Toast.makeText(applicationContext, "Opened messaging app (Zero-permission mode)", Toast.LENGTH_SHORT).show()
                                            }.onFailure { err ->
                                                Toast.makeText(applicationContext, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onExportJsonClicked = {
                                        exportLauncher.launch("remind_backup_${LocalDate.now()}.json")
                                    },
                                    onImportJsonClicked = {
                                        importLauncher.launch(arrayOf("application/json", "text/*"))
                                    },
                                    onSyncFirebaseNow = {
                                        scope.launch(Dispatchers.IO) {
                                            val fb = FirebaseBackupManager(applicationContext)
                                            val s = database.contactScheduleDao().getAllSchedules()
                                            val p = database.phraseDao().getAllPhrases()
                                            val l = database.messageLogDao().getPendingLogs()
                                            val res = fb.backupData(s, p, l)
                                            withContext(Dispatchers.Main) {
                                                if (res.isSuccess) {
                                                    Toast.makeText(applicationContext, "Cloud backup synced to Firestore!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(applicationContext, res.exceptionOrNull()?.message ?: "Sync failed (offline)", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    onOpenAndroidSettings = { openAppInfoSettings() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openAppInfoSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}
