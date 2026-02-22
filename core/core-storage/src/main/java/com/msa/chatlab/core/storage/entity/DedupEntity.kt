package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dedup")
data class DedupEntity(
    @PrimaryKey val id: String,
    val timestamp: Long
)
