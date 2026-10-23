package com.hallauer.remind.data.local.entity

enum class GroupBucket(
    val displayName: String,
    val allowsLoveWord: Boolean,
    val darkColorHex: Long,
    val lightColorHex: Long
) {
    SPOUSE("Spouse", allowsLoveWord = true, darkColorHex = 0xFFE11D48, lightColorHex = 0xFFBE123C),
    FAMILY("Family", allowsLoveWord = true, darkColorHex = 0xFFD97706, lightColorHex = 0xFFB45309),
    FRIENDS("Friends", allowsLoveWord = false, darkColorHex = 0xFF2563EB, lightColorHex = 0xFF1D4ED8),
    ACQUAINTANCES("Acquaintances", allowsLoveWord = false, darkColorHex = 0xFF0D9488, lightColorHex = 0xFF0F766E),
    OTHERS("Others", allowsLoveWord = false, darkColorHex = 0xFF7C3AED, lightColorHex = 0xFF6D28D9),
    COLLEAGUES("Colleagues", allowsLoveWord = false, darkColorHex = 0xFF06B6D4, lightColorHex = 0xFF0891B2),
    MENTORS("Mentors", allowsLoveWord = false, darkColorHex = 0xFF8B5CF6, lightColorHex = 0xFF7C3AED),
    CUSTOM("Custom", allowsLoveWord = false, darkColorHex = 0xFFF59E0B, lightColorHex = 0xFFD97706)
}

enum class RecurrenceType {
    DAILY,
    EVERY_N_DAYS,
    WEEKLY,
    EVERY_N_WEEKS,
    MONTHLY_DAY,
    YEARLY_DATE
}

enum class TimeMode {
    EXACT_TIME,          // Fires at targetHour:targetMinute
    RANDOM_WINDOW_1HR    // Picks a random minute 0..59 inside targetHour:00..targetHour:59
}

enum class MessageStatus {
    PENDING,             // Locked-in Next-1 occurrence waiting to fire
    SENT,                // Successfully dispatched via SmsManager
    SKIPPED_COLLISION,   // Skipped because user already texted this person earlier that calendar day
    SKIPPED_MANUAL,      // User tapped "Skip This Occurrence"
    FAILED               // SmsManager error or blocked by guardrail
}
