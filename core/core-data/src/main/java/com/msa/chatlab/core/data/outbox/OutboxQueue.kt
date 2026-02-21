package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

interface OutboxQueue {

    suspend fun enqueue(item: OutboxItem)

    suspend fun peekOldestPending(profileId: String): OutboxItem?

    suspend fun remove(profileId: String, messageId: String)

    suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: OutboxStatus,
        error: String?
    )

    fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItem>>

    fun observeCount(profileId: String, status: OutboxStatus): Flow<Int>

    suspend fun retryAllFailed(profileId: String)
    suspend fun clearFailed(profileId: String)
}
