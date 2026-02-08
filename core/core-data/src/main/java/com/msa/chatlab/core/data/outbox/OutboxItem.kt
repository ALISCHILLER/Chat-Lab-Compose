package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload

data class OutboxItem(
    val id: String,
    val payload: OutgoingPayload,
    val createdAt: Long,
    val attempt: Int = 0,
    val lastError: String? = null
)
