package com.msa.chatlab.core.data.mapper

import com.msa.chatlab.core.data.codec.StandardEnvelopeCodec
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload
import java.util.UUID

/**
 * Decodes a raw string message from the transport layer into a [TransportEvent].
 *
 * This function first attempts to decode the string as a structured [StandardEnvelopeCodec] message.
 * If parsing fails, it falls back to treating the raw string as a plain text message body.
 * This provides robustness for transports that may send either enveloped or raw messages.
 *
 * @return [TransportEvent.MessageReceived] The decoded message event.
 */
fun String.decodeToTransportEvent(): TransportEvent.MessageReceived {
    val envelope = StandardEnvelopeCodec.decodeOrNull(this)
    val incomingPayload = if (envelope != null) {
        IncomingPayload(envelope = envelope)
    } else {
        // Fallback: treat the entire string as a plain text message
        val newEnvelope = Envelope(
            messageId = MessageId(UUID.randomUUID().toString()),
            createdAt = TimestampMillis(System.currentTimeMillis()),
            contentType = "text/plain",
            headers = emptyMap(),
            body = this.encodeToByteArray()
        )
        IncomingPayload(envelope = newEnvelope)
    }
    return TransportEvent.MessageReceived(payload = incomingPayload)
}
