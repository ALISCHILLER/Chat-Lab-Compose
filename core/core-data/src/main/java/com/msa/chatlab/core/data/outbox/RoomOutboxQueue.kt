package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
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
            createdAt = TimestampMillis(createdAtMs),
            contentType = contentType,
            headers = headersJson.decodeHeaders(),
            body = body
        )
        return OutboxItem(
            id = id,
            payload = OutgoingPayload(envelope = env, destination = destination),
            createdAt = createdAtMs,
            attempt = attempt,
            lastError = lastError
        )
    }

    private fun OutboxItem.toEntity(): OutboxItemEntity {
        val env = payload.envelope
        return OutboxItemEntity(
            id = id,
            messageId = env.messageId.value,
            createdAtMs = env.createdAt.value,
            contentType = env.contentType,
            headersJson = env.headers.encodeHeaders(),
            body = env.body,
            destination = payload.destination,
            attempt = attempt,
            lastError = lastError
        )
    }

    private fun Map<String, String>.encodeHeaders(): String {
        return entries.joinToString("&") { "${it.key}=${it.value}" }
    }

    private fun String.decodeHeaders(): Map<String, String> {
        if (isBlank()) return emptyMap()
        return split("&")
            .mapNotNull {
                val idx = it.indexOf("=")
                if (idx <= 0) null else it.substring(0, idx) to it.substring(idx + 1)
            }
            .toMap()
    }
}
