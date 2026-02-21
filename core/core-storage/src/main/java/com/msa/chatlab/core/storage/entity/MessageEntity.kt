package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "messages",
    primaryKeys = ["profile_id", "message_id"],
    indices = [
        Index(value = ["profile_id", "created_at"]),
        Index(value = ["profile_id", "destination", "created_at"])
    ]
)
data class MessageEntity(
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "message_id") val messageId: String,

    @ColumnInfo(name = "direction") val direction: String, // OUT / IN
    @ColumnInfo(name = "destination") val destination: String = "default",
    @ColumnInfo(name = "source") val source: String? = null,

    @ColumnInfo(name = "content_type") val contentType: String = "text/plain",
    @ColumnInfo(name = "headers_json") val headersJson: String = "{}",

    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,

    @ColumnInfo(name = "status") val status: String = "SENT",
    @ColumnInfo(name = "queued") val queued: Boolean = false,
    @ColumnInfo(name = "attempt") val attempt: Int = 0,

    @ColumnInfo(name = "last_error") val lastError: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = createdAt
)