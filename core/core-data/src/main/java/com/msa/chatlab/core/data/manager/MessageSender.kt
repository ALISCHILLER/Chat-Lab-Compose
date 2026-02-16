package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class MessageSender(
    private val connectionManager: ConnectionManager,
    private val outboxQueue: OutboxQueue
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sendText(text: String, destination: String) {
        scope.launch {
            val messageId = UUID.randomUUID().toString()

            val item = OutboxItem(
                id = UUID.randomUUID().toString(),
                messageId = messageId,
                destination = destination,
                contentType = "text/plain",
                headersJson = "{}",
                body = text.encodeToByteArray(),
                attempt = 0,
                createdAt = System.currentTimeMillis()
            )

            if (!connectionManager.isConnectedNow()) {
                outboxQueue.enqueue(item)
                return@launch
            }

            try {
                val envelope = Envelope.text(text, MessageId(messageId))
                val payload = OutgoingPayload(envelope, destination)
                connectionManager.send(payload)
            } catch (t: Throwable) {
                outboxQueue.enqueue(item.copy(lastError = t.message))
            }
        }
    }
}
