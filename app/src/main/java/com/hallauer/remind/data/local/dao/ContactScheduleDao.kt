package com.hallauer.remind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.GroupBucket
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactScheduleDao {
    @Query("SELECT * FROM contact_schedules ORDER BY displayName ASC")
    fun getAllSchedulesFlow(): Flow<List<ContactScheduleEntity>>

    @Query("SELECT * FROM contact_schedules")
    suspend fun getAllSchedules(): List<ContactScheduleEntity>

    @Query("SELECT * FROM contact_schedules WHERE isActive = 1")
    suspend fun getActiveSchedules(): List<ContactScheduleEntity>

    @Query("SELECT * FROM contact_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ContactScheduleEntity?

    @Query("SELECT * FROM contact_schedules WHERE bucket = :bucket ORDER BY displayName ASC")
    fun getSchedulesByBucket(bucket: GroupBucket): Flow<List<ContactScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ContactScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ContactScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ContactScheduleEntity)

    @Query("UPDATE contact_schedules SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveState(id: Long, isActive: Boolean)

    @Query("UPDATE contact_schedules SET isActive = 0, pausedReason = :reason WHERE id = :id")
    suspend fun pauseScheduleWithReason(id: Long, reason: String)

    @Query("UPDATE contact_schedules SET lastManualInteractionEpochMillis = :timestamp WHERE normalizedNumber = :normalizedNumber")
    suspend fun updateLastInteraction(normalizedNumber: String, timestamp: Long)
}
