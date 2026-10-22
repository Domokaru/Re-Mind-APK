package com.hallauer.remind.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "remind_settings")

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_GLOBAL_PAUSE = booleanPreferencesKey("global_pause_all")
        val KEY_LAST_FIREBASE_SYNC = longPreferencesKey("last_firebase_sync_millis")
        val KEY_PRIMARY_SIM_ID = intPreferencesKey("primary_sim_id")
        val KEY_WEEKLY_BACKUP_ENABLED = booleanPreferencesKey("weekly_backup_enabled")
    }

    val themeModeFlow: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME_MODE] ?: ThemeMode.DARK.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.DARK)
    }

    val isGlobalPauseEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_GLOBAL_PAUSE] ?: false
    }

    val lastFirebaseSyncEpochFlow: Flow<Long> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_LAST_FIREBASE_SYNC] ?: 0L
    }

    val isWeeklyBackupEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_WEEKLY_BACKUP_ENABLED] ?: true
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { it[KEY_THEME_MODE] = themeMode.name }
    }

    suspend fun setGlobalPause(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_GLOBAL_PAUSE] = enabled }
    }

    suspend fun isGlobalPauseEnabled(): Boolean {
        return isGlobalPauseEnabledFlow.first()
    }

    suspend fun setLastFirebaseSync(timestampMillis: Long) {
        context.settingsDataStore.edit { it[KEY_LAST_FIREBASE_SYNC] = timestampMillis }
    }

    suspend fun setWeeklyBackupEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_WEEKLY_BACKUP_ENABLED] = enabled }
    }
}
