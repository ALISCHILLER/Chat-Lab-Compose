package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "runs",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["startedAt"])
    ]
)
data class RunEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "profileId") val profileId: String,
    val protocolType: String,
    val scenarioPreset: String,
    val startedAt: Long,
    val endedAt: Long,
    val summaryJson: String
)
