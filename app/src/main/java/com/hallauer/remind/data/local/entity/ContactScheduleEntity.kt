package com.hallauer.remind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_schedules")
data class ContactScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactLookupKey: String,           // Persistent ContactsContract.Contacts.LOOKUP_KEY
    val contactId: Long,
    val displayName: String,
    val phoneNumber: String,                // Selected target number (Mobile by default)
    val normalizedNumber: String,           // E.164 format for fast SMS/RCS collision matching
    val photoUri: String? = null,
    val bucket: GroupBucket,
    val recurrenceType: RecurrenceType,
    val intervalN: Int = 1,                 // Used for EVERY_N_DAYS (e.g. 3) or EVERY_N_WEEKS (e.g. 5)
    val daysOfWeekCsv: String = "",         // Used for WEEKLY / EVERY_N_WEEKS (ISO-8601 1=Mon..7=Sun, e.g. "1,4")
    val dayOfMonth: Int = 1,                // Used for MONTHLY_DAY (1..31, or -1 for Last Day) & YEARLY_DATE
    val monthOfYear: Int = 1,               // Used for YEARLY_DATE (1..12)
    val timeMode: TimeMode,
    val targetHour: Int,                    // Restricted to 7..22 (7:00 AM to 10:00 PM start hour)
    val targetMinute: Int = 0,              // 0..59 (used when timeMode == EXACT_TIME)
    val isActive: Boolean = true,
    val pausedReason: String? = null,       // Populated if Monthly Contact Verify detects changed/deleted number
    val lastSentPhraseId: Long? = null,     // Enforces N != N-1 non-repeating rule
    val lastSentPhraseText: String? = null,
    val lastDispatchedDateIso: String? = null, // "YYYY-MM-DD" of last actual dispatch (prevents double-text on 7 AM recovery)
    val lastManualInteractionEpochMillis: Long = 0L, // Updated by NotificationListenerService for RCS
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
