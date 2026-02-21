package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.storage.entity.OutboxStatus
import java.util.UUID

class MessageSender(
    private val activeProfileStore: ActiveProfileStore,
    private val messageRepository: MessageRepository,
    private val outboxQueue: OutboxQueue
) {
    suspend fun sendText(text: String, destination: String) {
        val profile = activeProfileStore.getActiveNow() ?: error("No active profile")

        val messageId = MessageId(UUID.randomUUID().toString())

        // 1) OUT message goes to DB
        messageRepository.insertOutgoing(
            profileId = profile.id,
            messageId = messageId,
            text = text,
            destination = destination
        )

        // 2) enqueue outbox (processor will send when connected)
        outboxQueue.enqueue(
            OutboxItem(
                profileId = profile.id.value,
                messageId = messageId.value,
                destination = destination,
                contentType = "text/plain",
                headersJson = "{}",
                body = text.encodeToByteArray(),
                createdAt = System.currentTimeMillis(),
                attempt = 0,
                lastError = null,
                status = OutboxStatus.PENDING
            )
        )
    }
}
