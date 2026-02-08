package com.msa.chatlab.core.protocolapi.payload

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis

data class Envelope(
    val messageId: MessageId,
    val createdAt: TimestampMillis,
    val contentType: String = "text/plain",
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray
)
