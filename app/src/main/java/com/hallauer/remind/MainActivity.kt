package com.hallauer.remind

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.hallauer.remind.core.util.RecurrenceCalculator
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.local.datastore.SettingsDataStore
import com.hallauer.remind.data.local.datastore.ThemeMode
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            val database = remember { RemindDatabase.getInstance(applicationContext, scope) }
            val repository = remember {
                RemindRepository(
                    context = applicationContext,
                    contactScheduleDao = database.contactScheduleDao(),
                    phraseDao = database.phraseDao(),
                    messageLogDao = database.messageLogDao()
                )
            }
            val settingsDataStore = remember { SettingsDataStore(applicationContext) }

            val themeMode by settingsDataStore.themeModeFlow.collectAsState(initial = ThemeMode.DARK)
            val isGlobalPause by settingsDataStore.isGlobalPauseEnabledFlow.collectAsState(initial = false)

            val schedules by repository.allSchedulesFlow.collectAsState(initial = emptyList())
            val pendingLogs by repository.pendingLogsFlow.collectAsState(initial = emptyList())
            val historyLogs by repository.executedHistoryFlow.collectAsState(initial = emptyList())
            val allPhrases by repository.allPhrasesFlow.collectAsState(initial = emptyList())

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

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
                                        selectedIconColor = Color(0xFF0F172A),
                                        selectedTextColor = Color(0xFF38BDF8),
                                        indicatorColor = Color(0xFF38BDF8),
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
                                    onSendNow = { log ->
                                        scope.launch {
                                            val sched = schedules.find { it.id == log.contactScheduleId }
                                            if (sched != null) repository.preRollNextOccurrence(sched)
                                        }
                                    },
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
                                    onSendNow = { log ->
                                        scope.launch {
                                            val sched = schedules.find { it.id == log.contactScheduleId }
                                            if (sched != null) repository.preRollNextOccurrence(sched)
                                        }
                                    },
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
                                    onThemeChanged = { mode ->
                                        scope.launch { settingsDataStore.setThemeMode(mode) }
                                    },
                                    onGlobalPauseToggled = { paused ->
                                        scope.launch { settingsDataStore.setGlobalPause(paused) }
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
