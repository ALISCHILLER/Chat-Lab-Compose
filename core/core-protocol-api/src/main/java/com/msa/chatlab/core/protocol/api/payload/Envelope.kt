package com.msa.chatlab.core.protocol.api.payload

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis

data class Envelope(
    val messageId: MessageId,
    val createdAt: TimestampMillis,
    val contentType: String,
    val headers: Map<String, String>,
    val body: ByteArray
) {
    companion object {
        fun text(text: String): Envelope = Envelope(
            messageId = MessageId("out-${System.nanoTime()}"),
            createdAt = TimestampMillis(System.currentTimeMillis()),
            contentType = "text/plain",
            headers = emptyMap(),
            body = text.encodeToByteArray()
        )
    }
}
