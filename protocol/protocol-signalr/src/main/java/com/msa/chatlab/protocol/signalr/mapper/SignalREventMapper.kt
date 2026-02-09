package com.msa.chatlab.protocol.signalr.mapper

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload

fun Any.toIncomingPayload(hubMethodName: String): IncomingPayload {
    val body = this.toString().encodeToByteArray()
    val envelope = Envelope(
        messageId = MessageId("signalr-${System.nanoTime()}"),
        createdAt = TimestampMillis(System.currentTimeMillis()),
        contentType = "text/plain",
        headers = emptyMap(),
        body = body
    )
    return IncomingPayload(envelope, hubMethodName)
}
