package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "runs",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["startedAt"]),
        Index(value = ["protocolType"])
    ]
)
data class RunEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val protocolType: String,
    val scenarioPreset: String,
    val startedAt: Long,
    val endedAt: Long,
    val summaryJson: String // خلاصه RunResult/metrics (فعلاً raw)
)
