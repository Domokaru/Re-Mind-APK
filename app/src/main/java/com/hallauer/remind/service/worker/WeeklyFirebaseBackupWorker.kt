package com.hallauer.remind.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.local.datastore.SettingsDataStore
import com.hallauer.remind.data.remote.FirebaseBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeeklyFirebaseBackupWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val settings = SettingsDataStore(appContext)
        val backupManager = FirebaseBackupManager(appContext)

        val db = RemindDatabase.getInstance(appContext, this)
        val schedules = db.contactScheduleDao().getAllSchedules()
        val phrases = db.phraseDao().getAllPhrases()
        val logs = db.messageLogDao().getPendingLogs()

        val backupResult = backupManager.backupData(schedules, phrases, logs)

        if (backupResult.isSuccess) {
            settings.setLastFirebaseSync(System.currentTimeMillis())
            Result.success()
        } else {
            // Gracefully succeed without failing worker if offline
            Result.success()
        }
    }
}
