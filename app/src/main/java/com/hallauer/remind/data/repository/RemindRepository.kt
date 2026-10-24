package com.hallauer.remind.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.core.util.CalculatedOccurrence
import com.hallauer.remind.core.util.LoveGuardrailValidator
import com.hallauer.remind.core.util.RecurrenceCalculator
import com.hallauer.remind.data.local.dao.ContactScheduleDao
import com.hallauer.remind.data.local.dao.MessageLogDao
import com.hallauer.remind.data.local.dao.PhraseDao
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.MessageStatus
import com.hallauer.remind.data.local.entity.PhraseEntity
import com.hallauer.remind.service.DispatchAlarmReceiver
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

class RemindRepository(
    private val context: Context,
    private val contactScheduleDao: ContactScheduleDao,
    private val phraseDao: PhraseDao,
    private val messageLogDao: MessageLogDao
) {
    val allSchedulesFlow: Flow<List<ContactScheduleEntity>> = contactScheduleDao.getAllSchedulesFlow()
    val pendingLogsFlow: Flow<List<MessageLogEntity>> = messageLogDao.getPendingLogsFlow()
    val executedHistoryFlow: Flow<List<MessageLogEntity>> = messageLogDao.getExecutedHistoryFlow()
    val allPhrasesFlow: Flow<List<PhraseEntity>> = phraseDao.getAllPhrasesFlow()

    fun getPhrasesByBucketFlow(bucket: GroupBucket): Flow<List<PhraseEntity>> =
        phraseDao.getPhrasesByBucketFlow(bucket)

    // ==================== PRE-ROLL & OCCURRENCE ENGINE ====================

    /**
     * Pre-rolls exactly one locked-in PENDING occurrence for a schedule,
     * picks a non-repeating random phrase (N != N-1), and sets the exact hardware alarm.
     */
    suspend fun preRollNextOccurrence(schedule: ContactScheduleEntity): MessageLogEntity? {
        if (!schedule.isActive) {
            cancelAlarm(schedule.id)
            messageLogDao.deletePendingForSchedule(schedule.id)
            return null
        }

        // 1. Calculate target date and time (respecting blackout clamping and 7:00 AM double-text prevention)
        val calculated: CalculatedOccurrence = RecurrenceCalculator.computeNextOccurrence(schedule)

        // 2. Select phrase using non-repeating N != N-1 rule
        val selectedPhrase = selectNonRepeatingPhrase(
            bucket = schedule.bucket,
            lastPhraseId = schedule.lastSentPhraseId,
            lastPhraseText = schedule.lastSentPhraseText
        )

        val phraseText = selectedPhrase?.text
            ?: "Thinking of you today. Hope you are having a steady, good week!"

        // 3. Clear existing pending row if any
        messageLogDao.deletePendingForSchedule(schedule.id)

        // 4. Insert new locked-in PENDING row
        val pendingLog = MessageLogEntity(
            contactScheduleId = schedule.id,
            contactDisplayName = schedule.displayName,
            contactPhoneNumber = schedule.phoneNumber,
            bucket = schedule.bucket,
            phraseId = selectedPhrase?.id,
            messageText = phraseText,
            isCustomEdited = false,
            scheduledDateIso = calculated.dateIso,
            scheduledEpochMillis = calculated.epochMillis,
            status = MessageStatus.PENDING
        )

        val insertedId = messageLogDao.insertLog(pendingLog)
        val finalLog = pendingLog.copy(id = insertedId)

        // 5. Schedule exact hardware alarm
        scheduleExactAlarm(finalLog)
        return finalLog
    }

    /**
     * Non-repeating random phrase selection (N != N-1).
     * Excludes lastSentPhraseId unless bucket only has 1 phrase.
     */
    suspend fun selectNonRepeatingPhrase(
        bucket: GroupBucket,
        lastPhraseId: Long?,
        lastPhraseText: String?
    ): PhraseEntity? {
        val pool = phraseDao.getPhrasesByBucket(bucket)
        if (pool.isEmpty()) return null
        if (pool.size == 1) return pool.first()

        val filtered = pool.filter { phrase ->
            phrase.id != lastPhraseId && phrase.text != lastPhraseText
        }

        val selectionPool = if (filtered.isNotEmpty()) filtered else pool
        return selectionPool[Random.nextInt(selectionPool.size)]
    }

    /**
     * Rerolls the phrase for a single upcoming PENDING message.
     */
    suspend fun rerollPendingMessage(logId: Long): MessageLogEntity? {
        val log = messageLogDao.getLogById(logId) ?: return null
        if (log.status != MessageStatus.PENDING) return log

        val schedule = contactScheduleDao.getScheduleById(log.contactScheduleId)

        // Exclude both the last sent phrase AND the current pending phrase
        val pool = phraseDao.getPhrasesByBucket(log.bucket)
        if (pool.isEmpty()) return log

        val eligible = pool.filter { it.id != log.phraseId && it.id != schedule?.lastSentPhraseId }
        val chosen = (if (eligible.isNotEmpty()) eligible else pool).random()

        val updated = log.copy(
            phraseId = chosen.id,
            messageText = chosen.text,
            isCustomEdited = false
        )
        messageLogDao.updateLog(updated)
        return updated
    }

    // ==================== PHRASE CRUD & CASCADE RULES ====================

    /**
     * Section 7.3: Updates phrase and cascades changes to any active PENDING logs referencing it.
     */
    suspend fun updatePhraseAndCascade(phrase: PhraseEntity): Result<Unit> {
        val validation = LoveGuardrailValidator.validate(phrase.bucket, phrase.text)
        if (validation is LoveGuardrailValidator.ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage))
        }

        phraseDao.updatePhrase(phrase)

        // Cascade update to unedited PENDING rows referencing this phrase
        val affectedLogs = messageLogDao.getPendingLogsReferencingPhrase(phrase.id)
        for (pendingLog in affectedLogs) {
            messageLogDao.updateLog(pendingLog.copy(messageText = phrase.text))
        }
        return Result.success(Unit)
    }

    /**
     * Section 7.3: Deletes phrase and rerolls any PENDING log that had pre-rolled it.
     */
    suspend fun deletePhraseAndCascade(phrase: PhraseEntity) {
        val affectedLogs = messageLogDao.getPendingLogsReferencingPhrase(phrase.id)
        phraseDao.deletePhrase(phrase)

        for (pendingLog in affectedLogs) {
            rerollPendingMessage(pendingLog.id)
        }
    }

    suspend fun createPhrase(bucket: GroupBucket, text: String): Result<Long> {
        val validation = LoveGuardrailValidator.validate(bucket, text)
        if (validation is LoveGuardrailValidator.ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage))
        }
        val id = phraseDao.insertPhrase(PhraseEntity(bucket = bucket, text = text.trim()))
        return Result.success(id)
    }

    // ==================== MANUAL ACTION SHEET HANDLERS ====================

    suspend fun updatePendingTextMessage(logId: Long, newText: String): Result<Unit> {
        val log = messageLogDao.getLogById(logId) ?: return Result.failure(NoSuchElementException("Log not found"))
        val validation = LoveGuardrailValidator.validate(log.bucket, newText)
        if (validation is LoveGuardrailValidator.ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage))
        }

        val updated = log.copy(
            messageText = newText.trim(),
            isCustomEdited = true
        )
        messageLogDao.updateLog(updated)
        return Result.success(Unit)
    }

    suspend fun skipOccurrence(logId: Long) {
        val log = messageLogDao.getLogById(logId) ?: return
        val schedule = contactScheduleDao.getScheduleById(log.contactScheduleId) ?: return

        // Mark current as SKIPPED_MANUAL
        val skippedLog = log.copy(
            status = MessageStatus.SKIPPED_MANUAL,
            executedEpochMillis = System.currentTimeMillis(),
            statusNote = "Skipped: Manually skipped by user"
        )
        messageLogDao.updateLog(skippedLog)

        // Pre-roll the next occurrence immediately
        preRollNextOccurrence(schedule)
    }

    suspend fun reschedulePendingOccurrence(logId: Long, newEpochMillis: Long, newDateIso: String) {
        val log = messageLogDao.getLogById(logId) ?: return
        val updated = log.copy(
            scheduledEpochMillis = newEpochMillis,
            scheduledDateIso = newDateIso,
            statusNote = "Rescheduled by user"
        )
        messageLogDao.updateLog(updated)
        scheduleExactAlarm(updated)
    }

    suspend fun setContactActiveState(scheduleId: Long, isActive: Boolean) {
        contactScheduleDao.updateActiveState(scheduleId, isActive)
        val schedule = contactScheduleDao.getScheduleById(scheduleId) ?: return
        if (isActive) {
            preRollNextOccurrence(schedule)
        } else {
            cancelAlarm(scheduleId)
            messageLogDao.deletePendingForSchedule(scheduleId)
        }
    }

    // ==================== HARDWARE EXACT ALARM DISPATCH ====================

    private fun scheduleExactAlarm(pendingLog: MessageLogEntity) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, DispatchAlarmReceiver::class.java).apply {
                action = AppConstants.ACTION_DISPATCH_SMS
                putExtra(AppConstants.EXTRA_LOG_ID, pendingLog.id)
                putExtra(AppConstants.EXTRA_SCHEDULE_ID, pendingLog.contactScheduleId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pendingLog.contactScheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val targetEpoch = pendingLog.scheduledEpochMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pendingIntent)
                    } catch (se: SecurityException) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pendingIntent)
                    }
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pendingIntent)
            }
        } catch (t: Throwable) {
            android.util.Log.e("RemindRepository", "Failed to schedule alarm", t)
        }
    }

    fun cancelAlarm(scheduleId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DispatchAlarmReceiver::class.java).apply {
                action = AppConstants.ACTION_DISPATCH_SMS
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (t: Throwable) {
            android.util.Log.e("RemindRepository", "Failed to cancel alarm", t)
        }
    }
}
