package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import java.util.UUID

class MessageSender(
    private val connectionManager: ConnectionManager,
    private val outbox: OutboxQueue
) {

    suspend fun sendText(text: String, destination: String = "default") {
        val env = Envelope(
            messageId = MessageId(UUID.randomUUID().toString()),
            createdAt = TimestampMillis(System.currentTimeMillis()),
            contentType = "text/plain",
            headers = emptyMap(),
            body = text.encodeToByteArray()
        )

        val payload = OutgoingPayload(envelope = env, destination = destination)

        if (connectionManager.isConnectedNow()) {
            connectionManager.sendViaPreparedTransport(payload)
        } else {
            outbox.enqueue(
                OutboxItem(
                    id = UUID.randomUUID().toString(),
                    payload = payload,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
