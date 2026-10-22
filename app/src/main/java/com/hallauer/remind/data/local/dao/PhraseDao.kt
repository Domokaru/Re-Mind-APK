package com.hallauer.remind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases ORDER BY createdAtEpochMillis ASC")
    fun getAllPhrasesFlow(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases")
    suspend fun getAllPhrases(): List<PhraseEntity>

    @Query("SELECT * FROM phrases WHERE bucket = :bucket ORDER BY createdAtEpochMillis ASC")
    fun getPhrasesByBucketFlow(bucket: GroupBucket): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE bucket = :bucket")
    suspend fun getPhrasesByBucket(bucket: GroupBucket): List<PhraseEntity>

    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getPhraseById(id: Long): PhraseEntity?

    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun getPhraseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)

    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)

    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)
}
