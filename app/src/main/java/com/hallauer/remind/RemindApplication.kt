package com.hallauer.remind

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.core.constants.SeedPhrases
import com.hallauer.remind.core.notifications.SilentStatusNotifier
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.local.entity.PhraseEntity
import com.hallauer.remind.service.worker.MonthlyContactVerifyWorker
import com.hallauer.remind.service.worker.WeeklyFirebaseBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RemindApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize silent notification channels
        try {
            SilentStatusNotifier.initNotificationChannels(this)
        } catch (e: Throwable) {
            Log.e("RemindApp", "Error initializing notification channels", e)
        }

        // 2. Initialize Room Database and seed 50 phrases safely in background
        applicationScope.launch {
            try {
                val db = RemindDatabase.getInstance(this@RemindApplication)
                if (db.phraseDao().getPhraseCount() == 0) {
                    val initialPhrases = SeedPhrases.ALL_50_PHRASES.map { seed ->
                        PhraseEntity(
                            bucket = seed.bucket,
                            text = seed.text,
                            isSeed = true
                        )
                    }
                    db.phraseDao().insertPhrases(initialPhrases)
                    Log.i("RemindApp", "Seeded 50 initial phrases successfully.")
                }
            } catch (e: Throwable) {
                Log.e("RemindApp", "Error during phrase seeding", e)
            }
        }

        // 3. Schedule Periodic Workers
        try {
            schedulePeriodicBackgroundWorkers()
        } catch (e: Throwable) {
            Log.e("RemindApp", "Error scheduling periodic workers", e)
        }
    }

    private fun schedulePeriodicBackgroundWorkers() {
        try {
            val workManager = WorkManager.getInstance(this)

            // Weekly backup worker (every 7 days)
            val weeklyBackupRequest = PeriodicWorkRequestBuilder<WeeklyFirebaseBackupWorker>(7, TimeUnit.DAYS)
                .addTag(AppConstants.TAG_WEEKLY_BACKUP)
                .build()

            workManager.enqueueUniquePeriodicWork(
                AppConstants.TAG_WEEKLY_BACKUP,
                ExistingPeriodicWorkPolicy.KEEP,
                weeklyBackupRequest
            )

            // Monthly Contact Number Verification (every 30 days)
            val monthlyVerifyRequest = PeriodicWorkRequestBuilder<MonthlyContactVerifyWorker>(30, TimeUnit.DAYS)
                .addTag(AppConstants.TAG_MONTHLY_VERIFY)
                .build()

            workManager.enqueueUniquePeriodicWork(
                AppConstants.TAG_MONTHLY_VERIFY,
                ExistingPeriodicWorkPolicy.KEEP,
                monthlyVerifyRequest
            )
        } catch (e: Throwable) {
            Log.e("RemindApp", "WorkManager worker enqueue failed", e)
        }
    }
}
