package com.msa.chatlab.core.data.dedup

interface DedupStore {
    /**
     * @return true اگر باید پردازش شود (جدید است)، false اگر تکراری است
     */
    fun shouldProcess(key: String): Boolean
    fun prune()
    fun clear()
}