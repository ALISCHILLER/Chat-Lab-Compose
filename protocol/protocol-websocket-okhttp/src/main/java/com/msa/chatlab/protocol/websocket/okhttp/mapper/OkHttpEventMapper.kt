package com.msa.chatlab.protocol.websocket.okhttp.mapper

import com.msa.chatlab.core.data.codec.StandardEnvelopeCodec
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload
import java.util.UUID

object OkHttpEventMapper {

    fun incomingText(text: String, now: Long): IncomingPayload {
        val decoded = StandardEnvelopeCodec.decodeOrNull(text)
        val env = decoded ?: Envelope(
            messageId = MessageId(UUID.randomUUID().toString()),
            createdAt = TimestampMillis(now),
            contentType = "text/plain",
            headers = emptyMap(),
            body = text.encodeToByteArray()
        )
        return IncomingPayload(envelope = env, source = "ws")
    }

    fun incomingBinary(bytes: ByteArray, now: Long): IncomingPayload {
        val asText = runCatching { bytes.decodeToString() }.getOrNull()
        val decoded = asText?.let { StandardEnvelopeCodec.decodeOrNull(it) }

        val env = decoded ?: Envelope(
            messageId = MessageId(UUID.randomUUID().toString()),
            createdAt = TimestampMillis(now),
            contentType = "application/octet-stream",
            headers = emptyMap(),
            body = bytes
        )
        return IncomingPayload(envelope = env, source = "ws")
    }
}