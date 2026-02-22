package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

interface OutboxQueue {

    suspend fun enqueue(item: OutboxItem)

    suspend fun claimNextPending(profileId: String, leaseMs: Long): OutboxItem?

    // ✅ فاز 2.2: batch claim
    suspend fun claimPendingBatch(profileId: String, leaseMs: Long, limit: Int): List<OutboxItem>

    suspend fun remove(profileId: String, messageId: String)

    suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: OutboxStatus,
        error: String?
    )

    suspend fun requeueExpiredInflight(profileId: String, leaseMs: Long): Int

    suspend fun count(profileId: String, statuses: List<OutboxStatus>): Int

    fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItem>>
    fun observeCount(profileId: String, status: OutboxStatus): Flow<Int>

    suspend fun retryAllFailed(profileId: String)
    suspend fun clearFailed(profileId: String)
}