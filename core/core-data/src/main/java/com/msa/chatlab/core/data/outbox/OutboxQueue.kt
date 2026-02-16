package com.msa.chatlab.core.data.outbox

import kotlinx.coroutines.flow.Flow

interface OutboxQueue {
    suspend fun enqueue(item: OutboxItem)
    suspend fun peekOldest(): OutboxItem?
    suspend fun remove(id: String)
    suspend fun incrementAttempt(id: String, error: String)
    suspend fun markAsFailed(id: String, error: String)
    fun observe(): Flow<List<OutboxItem>>
}
