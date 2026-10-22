package com.hallauer.remind

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.core.notifications.SilentStatusNotifier
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.service.worker.MonthlyContactVerifyWorker
import com.hallauer.remind.service.worker.WeeklyFirebaseBackupWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class RemindApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize silent notification channels
        SilentStatusNotifier.initNotificationChannels(this)

        // 2. Initialize Room Database and seed 50 phrases
        RemindDatabase.getInstance(this, applicationScope)

        // 3. Schedule Periodic Workers
        schedulePeriodicBackgroundWorkers()
    }

    private fun schedulePeriodicBackgroundWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Weekly Firebase cloud sync (every 7 days)
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
    }
}
