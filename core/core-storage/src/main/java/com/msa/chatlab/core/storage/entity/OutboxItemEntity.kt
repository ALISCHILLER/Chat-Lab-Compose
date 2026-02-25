package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.msa.chatlab.core.domain.model.OutboxStatus
import kotlinx.datetime.Instant

@Entity(tableName = "outbox")
internal data class OutboxItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    val message: String,
    val status: OutboxStatus,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "sent_at")
    val sentAt: Instant? = null,
    val error: String? = null,
)
