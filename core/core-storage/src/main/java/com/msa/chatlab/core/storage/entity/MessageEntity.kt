package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "messages",
    primaryKeys = ["profile_id", "message_id"],
    indices = [
        Index(value = ["profile_id", "created_at"])
    ]
)
data class MessageEntity(
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "direction") val direction: String, // "OUT" / "IN"
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "queued") val queued: Boolean = false,
    @ColumnInfo(name = "attempt") val attempt: Int = 0
)
