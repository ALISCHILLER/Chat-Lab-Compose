package com.msa.chatlab.core.observability.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ring buffer حافظه‌ای برای لاگ‌ها (برای Monitor/Export).
 * - bounded => بدون رشد بی‌نهایت
 * - thread-safe با synchronized
 */
class InMemoryLogStore(
    private val capacity: Int = 500
) : LogStore {

    private val lock = Any()
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    override val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    override fun append(entry: LogEntry) {
        synchronized(lock) {
            val cur = _entries.value
            val next = if (cur.size < capacity) cur + entry else cur.drop(1) + entry
            _entries.value = next
        }
    }

    override fun clear() {
        synchronized(lock) { _entries.value = emptyList() }
    }
}
