package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

interface OutboxQueue {
    suspend fun enqueue(item: OutboxItem)
    suspend fun peekOldest(): OutboxItem?
    suspend fun remove(id: String)
    suspend fun incrementAttempt(id: String, error: String)
    suspend fun markAsFailed(id: String, error: String)
    fun observe(): Flow<List<OutboxItem>>
    fun observeByStatus(status: OutboxStatus): Flow<List<OutboxItem>>
    suspend fun retryAllFailed()
    suspend fun clearFailed()
}
