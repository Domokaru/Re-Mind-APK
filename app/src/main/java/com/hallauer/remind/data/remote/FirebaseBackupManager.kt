package com.hallauer.remind.data.remote

import android.content.Context
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.PhraseEntity

class FirebaseBackupManager(private val context: Context) {

    fun isFirebaseConfigured(): Boolean = false

    suspend fun verifyAuth(): Result<String> {
        return Result.failure(IllegalStateException("Cloud sync requires google-services.json setup. Please use the Local JSON Export & Import tool in Settings."))
    }

    suspend fun backupData(
        schedules: List<ContactScheduleEntity>,
        phrases: List<PhraseEntity>,
        logs: List<MessageLogEntity>
    ): Result<Unit> {
        return Result.failure(IllegalStateException("Cloud sync requires google-services.json setup. Please use the Local JSON Export & Import tool in Settings."))
    }
}
