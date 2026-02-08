package com.msa.chatlab.core.data.outbox

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OutboxItem(
    val id: String,
    val text: String,
    val attempt: Int = 0,
    val createdAtMs: Long = System.currentTimeMillis()
)

class OutboxQueue {

    private val _items = MutableStateFlow<List<OutboxItem>>(emptyList())
    val items: Flow<List<OutboxItem>> = _items.asStateFlow()

    fun observe(): Flow<List<OutboxItem>> = items

    fun enqueue(text: String) {
        val it = OutboxItem(
            id = "ob-${System.nanoTime()}",
            text = text,
            attempt = 0
        )
        _items.value = _items.value + it
    }

    fun markAttempt(id: String, attempt: Int) {
        _items.value = _items.value.map { if (it.id == id) it.copy(attempt = attempt) else it }
    }

    fun remove(id: String) {
        _items.value = _items.value.filterNot { it.id == id }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
