package com.msa.chatlab.core.storage.mapper

import com.msa.chatlab.core.storage.entity.RunEntity

object RunMapper {
    fun newEntity(
        id: String,
        profileId: String,
        protocolType: String,
        scenarioPreset: String,
        startedAt: Long,
        endedAt: Long,
        summaryJson: String
    ): RunEntity = RunEntity(
        id = id,
        profileId = profileId,
        protocolType = protocolType,
        scenarioPreset = scenarioPreset,
        startedAt = startedAt,
        endedAt = endedAt,
        summaryJson = summaryJson
    )
}
