package com.msa.chatlab.core.data.pipeline

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import java.util.UUID

class EnvelopeFactory(
    private val now: () -> Long = { System.currentTimeMillis() }
) {
    fun newTextEnvelope(
        text: String,
        contentType: String = "text/plain",
        headers: Map<String, String> = emptyMap()
    ): Envelope {
        return Envelope(
            messageId = MessageId(UUID.randomUUID().toString()),
            createdAt = TimestampMillis(now()),
            contentType = contentType,
            headers = headers,
            body = text.encodeToByteArray()
        )
    }
}
