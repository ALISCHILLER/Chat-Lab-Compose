package com.msa.chatlab.core.data.outbox

import kotlinx.coroutines.flow.Flow

interface OutboxQueue {
    fun observe(): Flow<List<OutboxItem>>
    suspend fun enqueue(item: OutboxItem)
    suspend fun update(item: OutboxItem)
    suspend fun remove(id: String)
    suspend fun peekOldest(): OutboxItem?
    suspend fun size(): Int
}
