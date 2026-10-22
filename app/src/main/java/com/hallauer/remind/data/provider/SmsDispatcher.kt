package com.hallauer.remind.data.provider

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.hallauer.remind.core.constants.AppConstants

class SmsDispatcher(private val context: Context) {

    fun dispatchSms(phoneNumber: String, messageText: String, logId: Long): Result<Unit> {
        return runCatching {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val subId = SmsManager.getDefaultSmsSubscriptionId()
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
                } else {
                    context.getSystemService(SmsManager::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val sentIntent = Intent(AppConstants.ACTION_SMS_SENT_STATUS).apply {
                setPackage(context.packageName)
                putExtra(AppConstants.EXTRA_LOG_ID, logId)
            }
            val sentPendingIntent = PendingIntent.getBroadcast(
                context,
                logId.toInt(),
                sentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (messageText.length > 160) {
                val parts = smsManager.divideMessage(messageText)
                val sentIntents = ArrayList<PendingIntent>().apply {
                    repeat(parts.size) { add(sentPendingIntent) }
                }
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, messageText, sentPendingIntent, null)
            }
        }
    }
}
