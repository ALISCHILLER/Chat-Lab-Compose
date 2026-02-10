package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val runId: String,
    val timestamp: Long,
    val type: String,
    val payloadJson: String
)
