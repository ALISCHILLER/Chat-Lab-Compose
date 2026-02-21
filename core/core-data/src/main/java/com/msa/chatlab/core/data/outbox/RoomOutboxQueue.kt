package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOutboxQueue(private val dao: OutboxDao) : OutboxQueue {

    override suspend fun enqueue(item: OutboxItem) {
        dao.upsert(item.toEntity())
    }

    override suspend fun peekOldestPending(profileId: String): OutboxItem? {
        return dao.getOldest(profileId, OutboxStatus.PENDING)?.toDomain()
    }

    override suspend fun remove(profileId: String, messageId: String) {
        dao.delete(profileId, messageId)
    }

    override suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: OutboxStatus,
        error: String?
    ) {
        val now = System.currentTimeMillis()
        dao.updateAttempt(
            profileId = profileId,
            messageId = messageId,
            attempt = attempt,
            status = status,
            error = error,
            lastAttemptAt = now,
            updatedAt = now
        )
    }

    override fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItem>> {
        return dao.observeByStatus(profileId, status).map { list -> list.map { it.toDomain() } }
    }

    override fun observeCount(profileId: String, status: OutboxStatus): Flow<Int> {
        return dao.observeCountByStatus(profileId, status)
    }

    override suspend fun retryAllFailed(profileId: String) {
        dao.updateStatusForAll(profileId, OutboxStatus.FAILED, OutboxStatus.PENDING, System.currentTimeMillis())
    }

    override suspend fun clearFailed(profileId: String) {
        dao.deleteByStatus(profileId, OutboxStatus.FAILED)
    }
}
