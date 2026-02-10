package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox_items")
data class OutboxItemEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,

    // Envelope fields
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "content_type") val contentType: String,
    @ColumnInfo(name = "headers_json") val headersJson: String,
    @ColumnInfo(name = "body") val body: ByteArray,

    // Destination
    @ColumnInfo(name = "destination") val destination: String?,

    // Retry info
    @ColumnInfo(name = "attempt") val attempt: Int,
    @ColumnInfo(name = "last_error") val lastError: String?
)
