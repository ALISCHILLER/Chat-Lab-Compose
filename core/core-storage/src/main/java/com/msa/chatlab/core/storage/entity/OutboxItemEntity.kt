package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

enum class OutboxStatus { PENDING, FAILED }

@Entity(
    tableName = "outbox",
    primaryKeys = ["profile_id", "message_id"],
    indices = [
        Index(value = ["profile_id", "status", "created_at"])
    ]
)
data class OutboxItemEntity(
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "message_id") val messageId: String,

    @ColumnInfo(name = "destination") val destination: String,
    @ColumnInfo(name = "content_type") val contentType: String,
    @ColumnInfo(name = "headers_json") val headersJson: String,

    @ColumnInfo(name = "body") val body: ByteArray,

    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "attempt") val attempt: Int = 0,

    @ColumnInfo(name = "last_attempt_at") val lastAttemptAt: Long? = null,
    @ColumnInfo(name = "last_error") val lastError: String? = null,

    @ColumnInfo(name = "status") val status: OutboxStatus = OutboxStatus.PENDING,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = createdAt
)
