package com.hallauer.remind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_logs",
    foreignKeys = [
        ForeignKey(
            entity = ContactScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactScheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contactScheduleId"),
        Index("status"),
        Index("scheduledEpochMillis")
    ]
)
data class MessageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactScheduleId: Long,
    val contactDisplayName: String,
    val contactPhoneNumber: String,
    val bucket: GroupBucket,
    val phraseId: Long?,                    // Null if user wrote a one-off custom edit
    val messageText: String,                // Locked-in pre-rolled phrase text
    val isCustomEdited: Boolean = false,
    val scheduledDateIso: String,           // "YYYY-MM-DD" original target calendar date
    val scheduledEpochMillis: Long,         // Exact epoch millis when AlarmManager will fire
    val executedEpochMillis: Long? = null,  // Populated when SENT, SKIPPED, or FAILED
    val status: MessageStatus,
    val statusNote: String? = null          // e.g., "Skipped: Already messaged earlier today"
)
