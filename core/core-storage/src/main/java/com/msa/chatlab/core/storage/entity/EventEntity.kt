package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["runId"]),
        Index(value = ["timestamp"]),
        Index(value = ["type"])
    ]
)
data class EventEntity(
    @PrimaryKey val id: String,
    val runId: String,
    val timestamp: Long,
    val type: String,       // Connected/Disconnected/MessageSent/...
    val payloadJson: String // raw json for event details
)
