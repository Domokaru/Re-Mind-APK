package com.hallauer.remind.core.constants

object AppConstants {
    // 11:00 PM to 7:00 AM Blackout Window
    const val BLACKOUT_START_HOUR = 23
    const val BLACKOUT_END_HOUR = 7
    const val MORNING_DISPATCH_HOUR = 7
    const val MORNING_DISPATCH_MINUTE = 0

    // Timing bounds
    const val MIN_TARGET_HOUR = 7
    const val MAX_TARGET_HOUR = 22

    // Notification Channel IDs
    const val CHANNEL_ID_SILENT_STATUS = "channel_silent_status"
    const val CHANNEL_NAME_SILENT_STATUS = "Re:MIND Silent Status & Diagnostics"

    // Action Intents
    const val ACTION_DISPATCH_SMS = "com.hallauer.remind.ACTION_DISPATCH_SMS"
    const val ACTION_SMS_SENT_STATUS = "com.hallauer.remind.ACTION_SMS_SENT_STATUS"
    const val EXTRA_LOG_ID = "extra_log_id"
    const val EXTRA_SCHEDULE_ID = "extra_schedule_id"

    // WorkManager Periodic tags
    const val TAG_WEEKLY_BACKUP = "weekly_firebase_backup"
    const val TAG_MONTHLY_VERIFY = "monthly_contact_verify"
}
