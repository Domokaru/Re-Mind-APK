package com.hallauer.remind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phrases",
    indices = [Index(value = ["bucket"])]
)
data class PhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bucket: GroupBucket,
    val text: String,
    val isSeed: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
