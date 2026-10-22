package com.hallauer.remind.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.repository.RemindRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndTimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = RemindDatabase.getInstance(context, scope)
                    val repository = RemindRepository(
                        context = context,
                        contactScheduleDao = db.contactScheduleDao(),
                        phraseDao = db.phraseDao(),
                        messageLogDao = db.messageLogDao()
                    )

                    val activeSchedules = db.contactScheduleDao().getActiveSchedules()
                    for (schedule in activeSchedules) {
                        repository.preRollNextOccurrence(schedule)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
