package com.msa.chatlab.protocol.socketio.mapper

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload

fun Any.toIncomingPayload(eventName: String): IncomingPayload {
    val body = when (this) {
        is String -> this.encodeToByteArray()
        is ByteArray -> this
        else -> this.toString().encodeToByteArray()
    }
    val envelope = Envelope(
        messageId = MessageId("socketio-${System.nanoTime()}"),
        createdAt = TimestampMillis(System.currentTimeMillis()),
        contentType = "text/plain",
        headers = emptyMap(),
        body = body
    )
    return IncomingPayload(envelope, eventName)
}
