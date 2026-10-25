package com.hallauer.remind.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hallauer.remind.MainActivity
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.data.provider.SmsDispatcher

object SilentStatusNotifier {

    fun initNotificationChannels(context: Context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val silentChannel = NotificationChannel(
                    AppConstants.CHANNEL_ID_SILENT_STATUS,
                    AppConstants.CHANNEL_NAME_SILENT_STATUS,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Silent alerts for paused contacts, guardrail aborts, and integrity checks"
                    setShowBadge(false)
                    enableVibration(false)
                    enableLights(false)
                }
                manager.createNotificationChannel(silentChannel)

                val alertChannel = NotificationChannel(
                    AppConstants.CHANNEL_ID_CHECKIN_ALERTS,
                    AppConstants.CHANNEL_NAME_CHECKIN_ALERTS,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders and 1-tap messaging actions for scheduled check-ins"
                    setShowBadge(true)
                    enableVibration(true)
                    enableLights(true)
                }
                manager.createNotificationChannel(alertChannel)
            }
        }
    }

    fun notifyReadyToSendSms(
        context: Context,
        contactName: String,
        phoneNumber: String,
        messageText: String,
        logId: Long
    ) {
        runCatching {
            initNotificationChannels(context)

            val dispatcher = SmsDispatcher(context)
            val smsIntent = dispatcher.createSendIntent(phoneNumber, messageText).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                logId.toInt() + 10000,
                smsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val text = "\"$messageText\""
            val notification = NotificationCompat.Builder(context, AppConstants.CHANNEL_ID_CHECKIN_ALERTS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Send Check-in to $contactName")
                .setContentText(text)
                .setSubText("Tap to Send in Messages")
                .setStyle(NotificationCompat.BigTextStyle().bigText("$text\n\nTap to send via your messaging app."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_send,
                    "Send via Messages",
                    pendingIntent
                )
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(logId.toInt() + 4000, notification)
        }
    }

    fun notifyPausedContact(context: Context, contactName: String, reason: String, scheduleId: Long) {
        runCatching {
            initNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AppConstants.EXTRA_SCHEDULE_ID, scheduleId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, AppConstants.CHANNEL_ID_SILENT_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Re:MIND — Schedule Paused: $contactName")
                .setContentText(reason)
                .setStyle(NotificationCompat.BigTextStyle().bigText(reason))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(scheduleId.toInt() + 1000, notification)
        }
    }

    fun notifyGuardrailBlocked(context: Context, contactName: String, reason: String, logId: Long) {
        runCatching {
            initNotificationChannels(context)

            val notification = NotificationCompat.Builder(context, AppConstants.CHANNEL_ID_SILENT_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Re:MIND — Guardrail Blocked Send")
                .setContentText("Message to $contactName was blocked: $reason")
                .setStyle(NotificationCompat.BigTextStyle().bigText("Message to $contactName was blocked: $reason"))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(logId.toInt() + 5000, notification)
        }
    }

    fun notifyMessageDispatched(context: Context, contactName: String, messageText: String, logId: Long) {
        runCatching {
            initNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                logId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, AppConstants.CHANNEL_ID_SILENT_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Re:MIND — Sent Check-in to $contactName")
                .setContentText("\"$messageText\"")
                .setStyle(NotificationCompat.BigTextStyle().bigText("\"$messageText\""))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(logId.toInt() + 2000, notification)
        }
    }

    fun notifyCollisionSkipped(context: Context, contactName: String, logId: Long) {
        runCatching {
            initNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                logId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val text = "Automated check-in was skipped because you already messaged $contactName earlier today."
            val notification = NotificationCompat.Builder(context, AppConstants.CHANNEL_ID_SILENT_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Re:MIND — Check-in Skipped: $contactName")
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(logId.toInt() + 3000, notification)
        }
    }
}
