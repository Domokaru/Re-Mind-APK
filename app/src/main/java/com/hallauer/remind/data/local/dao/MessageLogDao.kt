package com.hallauer.remind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageLogDao {
    @Query("SELECT * FROM message_logs WHERE status = 'PENDING' ORDER BY scheduledEpochMillis ASC")
    fun getPendingLogsFlow(): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs WHERE status = 'PENDING' ORDER BY scheduledEpochMillis ASC")
    suspend fun getPendingLogs(): List<MessageLogEntity>

    @Query("SELECT * FROM message_logs WHERE status = 'PENDING' AND contactScheduleId = :scheduleId LIMIT 1")
    suspend fun getPendingLogForSchedule(scheduleId: Long): MessageLogEntity?

    @Query("SELECT * FROM message_logs WHERE id = :id")
    suspend fun getLogById(id: Long): MessageLogEntity?

    @Query("SELECT * FROM message_logs WHERE status != 'PENDING' ORDER BY executedEpochMillis DESC")
    fun getExecutedHistoryFlow(): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs WHERE status != 'PENDING' ORDER BY executedEpochMillis DESC LIMIT :limit")
    fun getRecentHistoryFlow(limit: Int = 10): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs WHERE scheduledDateIso = :dateIso")
    suspend fun getLogsForDate(dateIso: String): List<MessageLogEntity>

    @Query("SELECT * FROM message_logs WHERE phraseId = :phraseId AND status = 'PENDING' AND isCustomEdited = 0")
    suspend fun getPendingLogsReferencingPhrase(phraseId: Long): List<MessageLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MessageLogEntity): Long

    @Update
    suspend fun updateLog(log: MessageLogEntity)

    @Delete
    suspend fun deleteLog(log: MessageLogEntity)

    @Query("DELETE FROM message_logs WHERE contactScheduleId = :scheduleId AND status = 'PENDING'")
    suspend fun deletePendingForSchedule(scheduleId: Long)
}
