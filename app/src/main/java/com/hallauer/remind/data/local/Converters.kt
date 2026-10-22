package com.hallauer.remind.data.local

import androidx.room.TypeConverter
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.MessageStatus
import com.hallauer.remind.data.local.entity.RecurrenceType
import com.hallauer.remind.data.local.entity.TimeMode

class Converters {
    @TypeConverter
    fun fromGroupBucket(bucket: GroupBucket): String = bucket.name

    @TypeConverter
    fun toGroupBucket(value: String): GroupBucket = runCatching {
        GroupBucket.valueOf(value)
    }.getOrDefault(GroupBucket.OTHERS)

    @TypeConverter
    fun fromRecurrenceType(type: RecurrenceType): String = type.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType = runCatching {
        RecurrenceType.valueOf(value)
    }.getOrDefault(RecurrenceType.DAILY)

    @TypeConverter
    fun fromTimeMode(mode: TimeMode): String = mode.name

    @TypeConverter
    fun toTimeMode(value: String): TimeMode = runCatching {
        TimeMode.valueOf(value)
    }.getOrDefault(TimeMode.EXACT_TIME)

    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = runCatching {
        MessageStatus.valueOf(value)
    }.getOrDefault(MessageStatus.PENDING)
}
