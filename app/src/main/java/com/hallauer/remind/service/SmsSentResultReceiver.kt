package com.hallauer.remind.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.hallauer.remind.core.constants.AppConstants
import com.hallauer.remind.data.local.RemindDatabase
import com.hallauer.remind.data.local.entity.MessageStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSentResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(AppConstants.EXTRA_LOG_ID, -1L)
        if (logId == -1L) return

        val resultCode = resultCode
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = RemindDatabase.getInstance(context, scope)
            val log = db.messageLogDao().getLogById(logId) ?: return@launch

            if (resultCode == Activity.RESULT_OK) {
                db.messageLogDao().updateLog(
                    log.copy(
                        status = MessageStatus.SENT,
                        executedEpochMillis = System.currentTimeMillis(),
                        statusNote = "Delivered via SMS carrier"
                    )
                )
            } else {
                val errorReason = when (resultCode) {
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic carrier failure"
                    SmsManager.RESULT_ERROR_NO_SERVICE -> "No cellular service"
                    SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio/Airplane mode turned off"
                    else -> "SMS delivery error code: $resultCode"
                }
                db.messageLogDao().updateLog(
                    log.copy(
                        status = MessageStatus.FAILED,
                        executedEpochMillis = System.currentTimeMillis(),
                        statusNote = errorReason
                    )
                )
            }
        }
    }
}
