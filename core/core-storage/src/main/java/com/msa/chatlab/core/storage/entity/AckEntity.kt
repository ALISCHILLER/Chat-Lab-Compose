package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "acks")
data class AckEntity(
    @PrimaryKey val id: String,
    val timestamp: Long
)
