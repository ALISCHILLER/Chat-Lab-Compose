package com.msa.chatlab.core.data.dedup

interface DedupStore {
    /**
     * @return true if the key is new and should be processed, false if it's a duplicate.
     */
    suspend fun shouldProcess(key: String): Boolean

    suspend fun prune()

    suspend fun clear()
}
