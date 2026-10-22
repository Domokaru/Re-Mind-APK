package com.hallauer.remind.data.provider

import android.content.Context
import android.net.Uri
import com.hallauer.remind.core.util.PhoneNumberNormalizer
import java.time.LocalDate
import java.time.ZoneId

class SmsCollisionDetector(private val context: Context) {

    /**
     * Dual-Layer Same-Day Collision Check:
     * Returns true if user has sent an SMS/MMS to this number today (Layer 1),
     * or if the RCS notification listener recorded user activity for this contact today (Layer 2).
     */
    fun hasUserMessagedContactToday(
        targetNormalizedNumber: String,
        lastManualInteractionEpochMillis: Long
    ): Boolean {
        val startOfTodayMillis = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Layer 2: Google Messages RCS notification interaction timestamp
        if (lastManualInteractionEpochMillis >= startOfTodayMillis) {
            return true
        }

        // Layer 1: Query content://sms/sent since 00:00:00 local time today
        val hasSentSms = querySentSmsToday(targetNormalizedNumber, startOfTodayMillis)
        if (hasSentSms) return true

        return false
    }

    private fun querySentSmsToday(targetNormalizedNumber: String, startOfTodayMillis: Long): Boolean {
        val uri = Uri.parse("content://sms/sent")
        val projection = arrayOf("address", "date")
        val selection = "date >= ?"
        val selectionArgs = arrayOf(startOfTodayMillis.toString())

        return runCatching {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "date DESC"
            )

            var matched = false
            cursor?.use { c ->
                val addressIdx = c.getColumnIndex("address")
                while (c.moveToNext()) {
                    val address = c.getString(addressIdx)
                    if (PhoneNumberNormalizer.areEqual(address, targetNormalizedNumber)) {
                        matched = true
                        break
                    }
                }
            }
            matched
        }.getOrDefault(false)
    }
}
