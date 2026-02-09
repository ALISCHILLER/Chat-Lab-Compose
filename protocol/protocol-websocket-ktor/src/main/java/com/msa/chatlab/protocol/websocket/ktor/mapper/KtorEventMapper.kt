package com.msa.chatlab.protocol.websocket.ktor.mapper

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload
import io.ktor.websocket.*

fun Frame.toIncomingPayload(): IncomingPayload? {
    return when (this) {
        is Frame.Text -> {
            val text = readText()
            val envelope = Envelope(
                messageId = MessageId("ktor-${System.nanoTime()}"),
                createdAt = TimestampMillis(System.currentTimeMillis()),
                contentType = "text/plain",
                headers = emptyMap(),
                body = text.encodeToByteArray()
            )
            IncomingPayload(envelope, "ktor-ws")
        }
        is Frame.Binary -> {
            val data = readBytes()
            val envelope = Envelope(
                messageId = MessageId("ktor-${System.nanoTime()}"),
                createdAt = TimestampMillis(System.currentTimeMillis()),
                contentType = "application/octet-stream",
                headers = emptyMap(),
                body = data
            )
            IncomingPayload(envelope, "ktor-ws")
        }
        else -> null
    }
}
