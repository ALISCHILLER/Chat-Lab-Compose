package com.msa.chatlab.core.observability.log

import kotlinx.coroutines.flow.StateFlow

interface LogStore {
    val entries: StateFlow<List<LogEntry>>
    fun append(entry: LogEntry)
    fun clear()
}
