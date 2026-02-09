package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MessageSender(
    private val connectionManager: ConnectionManager,
    private val outbox: OutboxQueue
) {
    private val _simulateOffline = MutableStateFlow(false)
    val simulateOffline = _simulateOffline.asStateFlow()

    fun setSimulateOffline(enabled: Boolean) {
        _simulateOffline.value = enabled
    }

    fun newMessageId(): MessageId = MessageId("m-${System.nanoTime()}")

    suspend fun sendText(text: String, destination: String = "default"): MessageId {
        val id = newMessageId()
        val offline = _simulateOffline.value || !connectionManager.isConnectedNow()
        if (offline) {
            outbox.enqueue(id, text)
            return id
        }
        forceSend(id, text, destination)
        return id
    }

    suspend fun forceSend(messageId: MessageId, text: String, destination: String) {
        val payload = OutgoingPayload(
            envelope = Envelope.text(text, messageId),
            destination = destination
        )
        connectionManager.send(payload)
    }
}
