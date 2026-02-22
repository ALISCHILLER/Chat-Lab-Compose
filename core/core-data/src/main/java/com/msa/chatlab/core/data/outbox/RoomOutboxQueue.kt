package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOutboxQueue(private val dao: OutboxDao) : OutboxQueue {

    override suspend fun enqueue(item: OutboxItem) {
        dao.upsert(item.toEntity())
    }

    override suspend fun claimNextPending(profileId: String, leaseMs: Long): OutboxItem? {
        return claimPendingBatch(profileId, leaseMs, limit = 1).firstOrNull()
    }

    override suspend fun claimPendingBatch(profileId: String, leaseMs: Long, limit: Int): List<OutboxItem> {
        val now = System.currentTimeMillis()
        return dao.claimPendingBatchTx(profileId = profileId, now = now, limit = limit).map { it.toDomain() }
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

    override suspend fun requeueExpiredInflight(profileId: String, leaseMs: Long): Int {
        val now = System.currentTimeMillis()
        val olderThan = now - leaseMs
        return dao.requeueExpired(
            profileId = profileId,
            fromStatus = OutboxStatus.IN_FLIGHT,
            toStatus = OutboxStatus.PENDING,
            olderThan = olderThan,
            updatedAt = now
        )
    }

    override suspend fun count(profileId: String, statuses: List<OutboxStatus>): Int {
        var total = 0
        for (s in statuses.distinct()) total += dao.countByStatus(profileId, s)
        return total
    }

    override fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItem>> {
        return dao.observeByStatus(profileId, status).map { it.map { e -> e.toDomain() } }
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