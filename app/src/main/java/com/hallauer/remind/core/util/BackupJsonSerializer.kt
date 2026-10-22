package com.hallauer.remind.core.util

import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.PhraseEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class DatabaseBackupDto(
    val exportTimestampEpochMillis: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val schedules: List<ContactScheduleBackupDto>,
    val phrases: List<PhraseBackupDto>,
    val logs: List<MessageLogBackupDto>
)

@Serializable
data class ContactScheduleBackupDto(
    val id: Long,
    val contactLookupKey: String,
    val contactId: Long,
    val displayName: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val bucket: String,
    val recurrenceType: String,
    val intervalN: Int,
    val daysOfWeekCsv: String,
    val dayOfMonth: Int,
    val monthOfYear: Int,
    val timeMode: String,
    val targetHour: Int,
    val targetMinute: Int,
    val isActive: Boolean,
    val lastSentPhraseText: String? = null
)

@Serializable
data class PhraseBackupDto(
    val id: Long,
    val bucket: String,
    val text: String,
    val isSeed: Boolean
)

@Serializable
data class MessageLogBackupDto(
    val id: Long,
    val contactScheduleId: Long,
    val contactDisplayName: String,
    val contactPhoneNumber: String,
    val bucket: String,
    val messageText: String,
    val scheduledDateIso: String,
    val scheduledEpochMillis: Long,
    val executedEpochMillis: Long? = null,
    val status: String,
    val statusNote: String? = null
)

object BackupJsonSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun serialize(backupDto: DatabaseBackupDto): String {
        return json.encodeToString(backupDto)
    }

    fun deserialize(rawJson: String): Result<DatabaseBackupDto> {
        return runCatching {
            json.decodeFromString<DatabaseBackupDto>(rawJson)
        }
    }
}
