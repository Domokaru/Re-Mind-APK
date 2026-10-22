package com.hallauer.remind.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.PhraseEntity
import kotlinx.coroutines.tasks.await

class FirebaseBackupManager(private val context: Context) {

    private fun isFirebaseConfigured(): Boolean {
        return runCatching {
            FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)
    }

    suspend fun verifyAuth(): Result<String> {
        if (!isFirebaseConfigured()) {
            return Result.failure(IllegalStateException("Firebase is not initialized (google-services.json may not be installed). Operating in offline-first mode."))
        }

        return runCatching {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.uid
            } else {
                // Anonymous or silent sign-in
                val result = auth.signInAnonymously().await()
                result.user?.uid ?: throw IllegalStateException("Failed to obtain user identity.")
            }
        }
    }

    suspend fun backupData(
        schedules: List<ContactScheduleEntity>,
        phrases: List<PhraseEntity>,
        logs: List<MessageLogEntity>
    ): Result<Unit> {
        if (!isFirebaseConfigured()) {
            return Result.failure(IllegalStateException("Firebase not configured. Use Local JSON Export."))
        }

        return runCatching {
            val uid = verifyAuth().getOrThrow()
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(uid)

            val payload = hashMapOf(
                "updatedAtMillis" to System.currentTimeMillis(),
                "scheduleCount" to schedules.size,
                "phraseCount" to phrases.size,
                "logCount" to logs.size,
                "schedules" to schedules.map { s ->
                    mapOf(
                        "id" to s.id,
                        "displayName" to s.displayName,
                        "phoneNumber" to s.phoneNumber,
                        "normalizedNumber" to s.normalizedNumber,
                        "bucket" to s.bucket.name,
                        "recurrenceType" to s.recurrenceType.name,
                        "intervalN" to s.intervalN,
                        "daysOfWeekCsv" to s.daysOfWeekCsv,
                        "dayOfMonth" to s.dayOfMonth,
                        "monthOfYear" to s.monthOfYear,
                        "timeMode" to s.timeMode.name,
                        "targetHour" to s.targetHour,
                        "targetMinute" to s.targetMinute,
                        "isActive" to s.isActive
                    )
                },
                "phrases" to phrases.map { p ->
                    mapOf(
                        "id" to p.id,
                        "bucket" to p.bucket.name,
                        "text" to p.text,
                        "isSeed" to p.isSeed
                    )
                }
            )

            userDoc.set(payload, SetOptions.merge()).await()
            Unit
        }
    }
}
