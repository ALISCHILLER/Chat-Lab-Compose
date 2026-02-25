package com.msa.chatlab.core.storage

import androidx.room.TypeConverter
import com.msa.chatlab.core.domain.model.OutboxStatus
import kotlinx.datetime.Instant

internal class Converters {
    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun toOutboxStatus(value: String) = enumValueOf<OutboxStatus>(value)

    @TypeConverter
    fun fromOutboxStatus(value: OutboxStatus) = value.name
}
