package com.msa.chatlab.core.data.outbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryOutboxQueue : OutboxQueue {

    private val mutex = Mutex()
    private val items = mutableListOf<OutboxItem>()
    private val flow = MutableStateFlow<List<OutboxItem>>(emptyList())

    override fun observe() = flow.asStateFlow()

    override suspend fun enqueue(item: OutboxItem) {
        mutex.withLock {
            items.add(item)
            flow.value = items.toList()
        }
    }

    override suspend fun update(item: OutboxItem) {
        mutex.withLock {
            val idx = items.indexOfFirst { it.id == item.id }
            if (idx >= 0) items[idx] = item
            flow.value = items.toList()
        }
    }

    override suspend fun remove(id: String) {
        mutex.withLock {
            items.removeAll { it.id == id }
            flow.value = items.toList()
        }
    }

    override suspend fun peekOldest(): OutboxItem? = mutex.withLock {
        items.firstOrNull()
    }

    override suspend fun size(): Int = mutex.withLock { items.size }
}
