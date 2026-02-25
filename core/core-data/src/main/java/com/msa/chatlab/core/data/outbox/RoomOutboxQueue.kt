package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.domain.model.OutboxStatus
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
        dao.updateAttempt(profileId, messageId, attempt, status, error)
    }

    override suspend fun requeueExpiredInflight(profileId: String, leaseMs: Long): Int {
        val now = System.currentTimeMillis()
        return dao.requeueStaleInflightTx(profileId, now - leaseMs)
    }

    override suspend fun count(profileId: String, statuses: List<OutboxStatus>): Int {
        return statuses.sumOf { dao.countByStatus(profileId, it) }
    }

    override fun observeCount(profileId: String, status: OutboxStatus): Flow<Int> {
        return dao.observeCountByStatus(profileId, status)
    }

    override fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItem>> {
        return dao.observeByStatus(profileId, status).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun clearFailed(profileId: String) {
        dao.deleteByStatus(profileId, OutboxStatus.FAILED)
    }

    override suspend fun retryAllFailed(profileId: String) {
        dao.updateStatus(profileId, OutboxStatus.FAILED, OutboxStatus.PENDING)
    }
}
