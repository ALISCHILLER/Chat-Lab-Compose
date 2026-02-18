package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.protocol.api.event.TransportEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

class ConnectionLogStore {
    private val logs = ConcurrentLinkedQueue<TransportEvent>()
    private val _logFlow = MutableStateFlow<List<TransportEvent>>(emptyList())
    val logFlow = _logFlow.asStateFlow()

    fun add(event: TransportEvent) {
        if (logs.size >= 200) {
            logs.poll() // Remove the oldest element
        }
        logs.add(event)
        _logFlow.value = logs.toList().reversed()
    }

    fun clear() {
        logs.clear()
        _logFlow.value = emptyList()
    }
}
