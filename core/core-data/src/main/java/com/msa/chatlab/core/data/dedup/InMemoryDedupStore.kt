package com.msa.chatlab.core.data.dedup

import java.util.concurrent.ConcurrentHashMap

class InMemoryDedupStore : DedupStore {

    private val processedKeys = ConcurrentHashMap.newKeySet<String>()

    override suspend fun shouldProcess(key: String): Boolean {
        return processedKeys.add(key)
    }

    override suspend fun prune() {
        // In a real implementation, you might have a timestamp-based eviction strategy.
    }

    override suspend fun clear() {
        processedKeys.clear()
    }
}
