package com.msa.chatlab.core.storage.mapper

import com.msa.chatlab.core.storage.entity.EventEntity

object EventMapper {
    fun newEntity(
        id: String,
        runId: String,
        timestamp: Long,
        type: String,
        payloadJson: String
    ): EventEntity = EventEntity(
        id = id,
        runId = runId,
        timestamp = timestamp,
        type = type,
        payloadJson = payloadJson
    )
}
