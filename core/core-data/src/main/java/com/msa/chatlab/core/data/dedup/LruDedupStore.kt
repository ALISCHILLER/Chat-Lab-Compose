/*
 * Copyright 2024 M. Sayed Alighaleh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.msa.chatlab.core.data.dedup

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedHashMap

class LruDedupStore(
    private val maxSize: Int = 5_000,
    private val ttlMs: Long = 60_000
) : DedupStore {

    private val mutex = Mutex()
    private val map = object : LinkedHashMap<String, Long>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>): Boolean {
            return size > maxSize
        }
    }

    override suspend fun shouldProcess(key: String): Boolean = mutex.withLock {
        val now = System.currentTimeMillis()
        val last = map[key]
        if (last != null && (now - last) <= ttlMs) {
            false
        } else {
            map[key] = now
            true
        }
    }

    override suspend fun prune(): Unit = mutex.withLock {
        val now = System.currentTimeMillis()
        val it = map.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if ((now - e.value) > ttlMs) it.remove()
        }
    }

    override suspend fun clear(): Unit = mutex.withLock {
        map.clear()
    }
}
