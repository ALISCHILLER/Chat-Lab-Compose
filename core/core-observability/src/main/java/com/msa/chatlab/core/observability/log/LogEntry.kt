package com.msa.chatlab.core.observability.log

data class LogEntry(
    val ts: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val context: Map<String, String> = emptyMap(),
    val throwable: Throwable? = null
)
