package com.hallauer.remind.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hallauer.remind.core.util.PhoneNumberNormalizer
import com.hallauer.remind.data.local.RemindDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagingInteractionListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val sbnNonNull = sbn ?: return
        val packageName = sbnNonNull.packageName

        // Layer 2: Detect Google Messages or native SMS/RCS notifications
        if (packageName == "com.google.android.apps.messaging" || packageName == "com.samsung.android.messaging") {
            val extras = sbnNonNull.notification.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            serviceScope.launch {
                val db = RemindDatabase.getInstance(applicationContext, serviceScope)
                val allSchedules = db.contactScheduleDao().getAllSchedules()

                for (schedule in allSchedules) {
                    val matchesName = title.contains(schedule.displayName, ignoreCase = true)
                    val matchesNumber = PhoneNumberNormalizer.areEqual(title, schedule.normalizedNumber) ||
                            PhoneNumberNormalizer.areEqual(text, schedule.normalizedNumber)

                    if (matchesName || matchesNumber) {
                        // User has an active conversation or incoming/outgoing interaction today
                        db.contactScheduleDao().updateLastInteraction(
                            normalizedNumber = schedule.normalizedNumber,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                }
            }
        }
    }
}
