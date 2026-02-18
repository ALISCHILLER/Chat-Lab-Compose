package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val durationSec: Int,
    val rps: Int,
    val payloadBytes: Int,
    val pattern: String // "Text" | "RandomAscii"
)
