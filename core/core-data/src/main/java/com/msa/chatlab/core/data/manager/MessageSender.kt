package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class MessageSender(
    private val connectionManager: ConnectionManager,
    private val outboxQueue: OutboxQueue,
    private val scope: CoroutineScope,
) {
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
                // NOTE: The `destination` parameter is currently ignored as the updated
                // ConnectionManager#send only accepts a ByteArray payload.
                connectionManager.send(text.encodeToByteArray())
            } catch (t: Throwable) {
                outboxQueue.enqueue(item.copy(lastError = t.message))
            }
        }
    }
}
