package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOutboxQueue(private val dao: OutboxDao) : OutboxQueue {
    override suspend fun enqueue(item: OutboxItem) {
        dao.insert(item.toEntity())
    }

    override suspend fun peekOldest(): OutboxItem? {
        return dao.getOldestPending()?.toDomain()
    }

    override suspend fun remove(id: String) {
        dao.deleteById(id)
    }

    override suspend fun incrementAttempt(id: String, error: String) {
        dao.incrementAttempt(id, System.currentTimeMillis(), error)
    }

    override suspend fun markAsFailed(id: String, error: String) {
        dao.updateStatus(id, OutboxStatus.FAILED, error)
    }

    override fun observe(): Flow<List<OutboxItem>> {
        return dao.observeAll().map { list -> list.map { it.toDomain() } }
    }
}
