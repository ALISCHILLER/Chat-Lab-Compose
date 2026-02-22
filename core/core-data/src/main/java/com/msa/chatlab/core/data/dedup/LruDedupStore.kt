package com.msa.chatlab.core.data.dedup

import java.util.LinkedHashMap

class LruDedupStore(
    private val maxSize: Int = 5_000,
    private val ttlMs: Long = 60_000
) : DedupStore {

    private val map = object : LinkedHashMap<String, Long>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>): Boolean {
            return size > maxSize
        }
    }

    @Synchronized
    override fun shouldProcess(key: String): Boolean {
        val now = System.currentTimeMillis()
        val last = map[key]
        if (last != null && (now - last) <= ttlMs) return false
        map[key] = now
        return true
    }

    @Synchronized
    override fun prune() {
        val now = System.currentTimeMillis()
        val it = map.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if ((now - e.value) > ttlMs) it.remove()
        }
    }

    @Synchronized
    override fun clear() {
        map.clear()
    }
}