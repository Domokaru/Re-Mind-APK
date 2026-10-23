package com.hallauer.remind.service.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.core.notifications.SilentStatusNotifier
import com.hallauer.remind.core.util.LoveGuardrailValidator
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.local.datastore.SettingsDataStore
import com.hallauer.remind.data.local.entity.MessageStatus
import com.hallauer.remind.data.provider.SmsCollisionDetector
import com.hallauer.remind.data.provider.SmsDispatcher
import com.hallauer.remind.data.repository.RemindRepository
import com.hallauer.remind.service.DispatchAlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class SmsDispatchWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val logId = inputData.getLong(AppConstants.EXTRA_LOG_ID, -1L)
        if (logId == -1L) return@withContext Result.failure()

        val db = RemindDatabase.getInstance(appContext, this)
        val messageLogDao = db.messageLogDao()
        val contactScheduleDao = db.contactScheduleDao()
        val repository = RemindRepository(appContext, contactScheduleDao, db.phraseDao(), messageLogDao)
        val settingsDataStore = SettingsDataStore(appContext)

        val pendingLog = messageLogDao.getLogById(logId) ?: return@withContext Result.failure()
        if (pendingLog.status != MessageStatus.PENDING) return@withContext Result.success()

        val schedule = contactScheduleDao.getScheduleById(pendingLog.contactScheduleId)
            ?: return@withContext Result.failure()

        // 1. Check Global Vacation Mode / Pause All
        if (settingsDataStore.isGlobalPauseEnabled()) {
            // Keep pending until unpaused
            return@withContext Result.success()
        }

        // 2. Check Contact Active State
        if (!schedule.isActive) {
            messageLogDao.deletePendingForSchedule(schedule.id)
            return@withContext Result.success()
        }

        // 3. Blackout Window Check (23:00 to 06:59:59)
        val now = LocalTime.now()
        if (now.hour >= AppConstants.BLACKOUT_START_HOUR || now.hour < AppConstants.BLACKOUT_END_HOUR) {
            val targetDate = if (now.hour >= AppConstants.BLACKOUT_START_HOUR) {
                LocalDate.now().plusDays(1)
            } else {
                LocalDate.now()
            }
            val nextSevenAm = LocalDateTime.of(targetDate, LocalTime.of(AppConstants.MORNING_DISPATCH_HOUR, AppConstants.MORNING_DISPATCH_MINUTE))
            val targetEpoch = nextSevenAm.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val postponedLog = pendingLog.copy(
                scheduledEpochMillis = targetEpoch,
                scheduledDateIso = targetDate.toString(),
                statusNote = "Queued for 7:00 AM (Blackout Window)"
            )
            messageLogDao.updateLog(postponedLog)

            // Reschedule exact alarm for 7:00 AM
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(appContext, DispatchAlarmReceiver::class.java).apply {
                action = AppConstants.ACTION_DISPATCH_SMS
                putExtra(AppConstants.EXTRA_LOG_ID, postponedLog.id)
                putExtra(AppConstants.EXTRA_SCHEDULE_ID, postponedLog.contactScheduleId)
            }
            val pi = PendingIntent.getBroadcast(
                appContext,
                postponedLog.contactScheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
            return@withContext Result.success()
        }

        // 4. Same-Day SMS/RCS Collision Check
        val collisionDetector = SmsCollisionDetector(appContext)
        val hasMessagedToday = collisionDetector.hasUserMessagedContactToday(
            targetNormalizedNumber = schedule.normalizedNumber,
            lastManualInteractionEpochMillis = schedule.lastManualInteractionEpochMillis
        )

        if (hasMessagedToday) {
            // Abort automated send to avoid awkward double-texting
            val skippedLog = pendingLog.copy(
                status = MessageStatus.SKIPPED_COLLISION,
                executedEpochMillis = System.currentTimeMillis(),
                statusNote = "Skipped: You already messaged ${schedule.displayName} today"
            )
            messageLogDao.updateLog(skippedLog)
            SilentStatusNotifier.notifyCollisionSkipped(appContext, schedule.displayName, pendingLog.id)

            // Update last dispatched date and pre-roll next occurrence
            contactScheduleDao.updateSchedule(
                schedule.copy(lastDispatchedDateIso = LocalDate.now().toString())
            )
            repository.preRollNextOccurrence(schedule)
            return@withContext Result.success()
        }

        // 5. Final Lexical "No-Love" Guardrail Check
        val guardrailResult = LoveGuardrailValidator.validate(schedule.bucket, pendingLog.messageText)
        if (guardrailResult is LoveGuardrailValidator.ValidationResult.Invalid) {
            val failedLog = pendingLog.copy(
                status = MessageStatus.FAILED,
                executedEpochMillis = System.currentTimeMillis(),
                statusNote = "Blocked by Guardrail: ${guardrailResult.errorMessage}"
            )
            messageLogDao.updateLog(failedLog)
            SilentStatusNotifier.notifyGuardrailBlocked(appContext, schedule.displayName, guardrailResult.errorMessage, pendingLog.id)
            return@withContext Result.failure()
        }

        // 6. Silent SMS Dispatch via Primary SIM
        val dispatcher = SmsDispatcher(appContext)
        val dispatchResult = dispatcher.dispatchSms(
            phoneNumber = schedule.phoneNumber,
            messageText = pendingLog.messageText,
            logId = pendingLog.id
        )

        if (dispatchResult.isSuccess) {
            val sentLog = pendingLog.copy(
                status = MessageStatus.SENT,
                executedEpochMillis = System.currentTimeMillis(),
                statusNote = "Dispatched via device SMS"
            )
            messageLogDao.updateLog(sentLog)
            SilentStatusNotifier.notifyMessageDispatched(appContext, schedule.displayName, pendingLog.messageText, pendingLog.id)

            // Update contact with last sent phrase and last dispatched date
            val updatedSchedule = schedule.copy(
                lastSentPhraseId = pendingLog.phraseId,
                lastSentPhraseText = pendingLog.messageText,
                lastDispatchedDateIso = LocalDate.now().toString()
            )
            contactScheduleDao.updateSchedule(updatedSchedule)

            // Immediately pre-roll next single occurrence
            repository.preRollNextOccurrence(updatedSchedule)
            Result.success()
        } else {
            val errorMsg = dispatchResult.exceptionOrNull()?.message ?: "Radio error"
            val failedLog = pendingLog.copy(
                status = MessageStatus.FAILED,
                executedEpochMillis = System.currentTimeMillis(),
                statusNote = "Dispatch failed: $errorMsg"
            )
            messageLogDao.updateLog(failedLog)
            Result.retry()
        }
    }
}
