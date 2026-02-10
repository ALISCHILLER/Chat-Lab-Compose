package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOutboxQueue(
    private val dao: OutboxDao
) : OutboxQueue {

    override fun observe(): Flow<List<OutboxItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

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
}

private fun OutboxItemEntity.toDomain() = OutboxItem(
    id = id,
    messageId = messageId,
    destination = destination,
    contentType = contentType,
    headersJson = headersJson,
    body = body,
    attempt = attempt,
    createdAt = createdAt,
    lastError = lastError
)

private fun OutboxItem.toEntity() = OutboxItemEntity(
    id = id,
    messageId = messageId,
    destination = destination,
    contentType = contentType,
    headersJson = headersJson,
    body = body,
    attempt = attempt,
    createdAt = createdAt,
    lastError = lastError
)
