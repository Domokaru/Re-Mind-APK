package com.hallauer.remind.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.service.worker.SmsDispatchWorker

class DispatchAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(AppConstants.EXTRA_LOG_ID, -1L)
        val scheduleId = intent.getLongExtra(AppConstants.EXTRA_SCHEDULE_ID, -1L)

        if (logId != -1L) {
            val inputData = Data.Builder()
                .putLong(AppConstants.EXTRA_LOG_ID, logId)
                .putLong(AppConstants.EXTRA_SCHEDULE_ID, scheduleId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SmsDispatchWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
