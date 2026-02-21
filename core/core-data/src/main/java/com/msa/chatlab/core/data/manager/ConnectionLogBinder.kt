package com.msa.chatlab.core.data.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ConnectionLogBinder(
    private val connectionManager: ConnectionManager,
    private val logStore: ConnectionLogStore,
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch {
            connectionManager.events.collect { ev ->
                logStore.add(ev)
            }
        }
    }
}
