package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.msa.chatlab.core.domain.model.OutboxStatus

@Entity(
    tableName = "outbox",
    primaryKeys = ["profile_id", "message_id"]
)
data class OutboxItemEntity(
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    @ColumnInfo(name = "message_id")
    val messageId: String,
    val destination: String,
    @ColumnInfo(name = "content_type")
    val contentType: String,
    @ColumnInfo(name = "headers_json")
    val headersJson: String,
    val body: ByteArray,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val attempt: Int = 0,
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null,
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
    val status: OutboxStatus = OutboxStatus.PENDING,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = createdAt,
)
