package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOutboxQueue(
    private val dao: OutboxDao
) : OutboxQueue {

    override fun observe(): Flow<List<OutboxItem>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun enqueue(item: OutboxItem) {
        dao.upsert(item.toEntity())
    }

    override suspend fun update(item: OutboxItem) {
        dao.upsert(item.toEntity())
    }

    override suspend fun remove(id: String) {
        dao.deleteById(id)
    }

    override suspend fun peekOldest(): OutboxItem? =
        dao.peekOldest()?.toDomain()

    override suspend fun size(): Int = dao.count()

    private fun OutboxItemEntity.toDomain(): OutboxItem {
        val env = Envelope(
            messageId = MessageId(messageId),
            createdAt = com.msa.chatlab.core.domain.value.TimestampMillis(createdAt),
            contentType = "text/plain", // Placeholder
            headers = emptyMap(), // Placeholder
            body = text.toByteArray()
        )
        return OutboxItem(
            id = id,
            payload = OutgoingPayload(envelope = env, destination = "default"), // Placeholder
            createdAt = createdAt,
            attempt = attempt,
            lastError = null // Placeholder
        )
    }

    private fun OutboxItem.toEntity(): OutboxItemEntity {
        val env = payload.envelope
        return OutboxItemEntity(
            id = id,
            messageId = env.messageId.value,
            text = String(env.body),
            attempt = attempt,
            createdAt = createdAt
        )
    }
}
