package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OutboxStatus {
    PENDING,
    FAILED
}

@Entity(tableName = "outbox")
data class OutboxItemEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val destination: String,
    val contentType: String,
    val headersJson: String,
    val body: ByteArray,
    val createdAt: Long,
    val attempt: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null,
    val status: OutboxStatus = OutboxStatus.PENDING
)
