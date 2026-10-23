package com.hallauer.remind.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hallauer.remind.core.notifications.SilentStatusNotifier
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.provider.ContactsProviderHelper
import com.hallauer.remind.data.repository.RemindRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MonthlyContactVerifyWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = RemindDatabase.getInstance(appContext, this)
        val contactScheduleDao = db.contactScheduleDao()
        val repository = RemindRepository(appContext, contactScheduleDao, db.phraseDao(), db.messageLogDao())
        val contactsHelper = ContactsProviderHelper(appContext)

        val activeSchedules = contactScheduleDao.getActiveSchedules()

        for (schedule in activeSchedules) {
            val verifyResult = contactsHelper.verifyContactNumber(
                lookupKey = schedule.contactLookupKey,
                expectedNormalizedNumber = schedule.normalizedNumber
            )

            when (verifyResult) {
                is ContactsProviderHelper.VerificationResult.Valid -> {
                    // All good
                }
                is ContactsProviderHelper.VerificationResult.ContactNotFound -> {
                    val reason = "Contact no longer exists in Google Contacts."
                    contactScheduleDao.pauseScheduleWithReason(schedule.id, reason)
                    repository.cancelAlarm(schedule.id)
                    SilentStatusNotifier.notifyPausedContact(appContext, schedule.displayName, reason, schedule.id)
                }
                is ContactsProviderHelper.VerificationResult.NumberChangedOrRemoved -> {
                    val reason = "Contact phone number changed or was removed in Google Contacts. Please review."
                    contactScheduleDao.pauseScheduleWithReason(schedule.id, reason)
                    repository.cancelAlarm(schedule.id)
                    SilentStatusNotifier.notifyPausedContact(appContext, schedule.displayName, reason, schedule.id)
                }
            }
        }

        Result.success()
    }
}
