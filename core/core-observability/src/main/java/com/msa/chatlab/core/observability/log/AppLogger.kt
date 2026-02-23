package com.msa.chatlab.core.observability.log

interface AppLogger {
    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        context: Map<String, String> = emptyMap(),
        throwable: Throwable? = null
    )
}

// Extension functions for convenience
fun AppLogger.v(tag: String, message: String, context: Map<String, String> = emptyMap(), throwable: Throwable? = null) =
    log(LogLevel.VERBOSE, tag, message, context, throwable)

fun AppLogger.d(tag: String, message: String, context: Map<String, String> = emptyMap(), throwable: Throwable? = null) =
    log(LogLevel.DEBUG, tag, message, context, throwable)

fun AppLogger.i(tag: String, message: String, context: Map<String, String> = emptyMap(), throwable: Throwable? = null) =
    log(LogLevel.INFO, tag, message, context, throwable)

fun AppLogger.w(tag: String, message: String, context: Map<String, String> = emptyMap(), throwable: Throwable? = null) =
    log(LogLevel.WARN, tag, message, context, throwable)

fun AppLogger.e(tag: String, message: String, context: Map<String, String> = emptyMap(), throwable: Throwable? = null) =
    log(LogLevel.ERROR, tag, message, context, throwable)
