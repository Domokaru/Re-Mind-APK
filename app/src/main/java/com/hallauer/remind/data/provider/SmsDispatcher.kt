package com.hallauer.remind.data.provider

import android.Manifest
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.hallauer.remind.core.constants.AppConstants

class SmsDispatcher(private val context: Context) {

    /**
     * Checks if the app is currently designated as the system Default SMS app.
     * When holding this role, Android automatically grants SEND_SMS, READ_SMS, and RECEIVE_SMS
     * natively without any Restricted Settings barriers.
     */
    fun isDefaultSmsApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        }
    }

    /**
     * Checks if the app has raw SEND_SMS permission (either granted directly or via default SMS role).
     */
    fun hasSendSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns true if the app can silently send SMS via hardware SIM without user intervention.
     */
    fun canSendSilently(): Boolean {
        return isDefaultSmsApp() || hasSendSmsPermission()
    }

    /**
     * Creates an intent to request the Default SMS role from the Android OS.
     */
    fun createRequestDefaultSmsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            @Suppress("DEPRECATION")
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
        }
    }

    /**
     * Creates the Android-compliant zero-permission intent that opens the user's default messaging app.
     */
    fun createSendIntent(phoneNumber: String, messageText: String): Intent {
        val cleanNumber = phoneNumber.trim()
        val uri = Uri.parse("smsto:${Uri.encode(cleanNumber)}")
        return Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", messageText)
            putExtra(Intent.EXTRA_TEXT, messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Sends SMS via device hardware radio when authorized.
     */
    fun dispatchSms(phoneNumber: String, messageText: String, logId: Long): Result<Unit> {
        return runCatching {
            if (!canSendSilently()) {
                throw SecurityException("SEND_SMS permission not available. Set as Default SMS app or use Intent mode.")
            }

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
