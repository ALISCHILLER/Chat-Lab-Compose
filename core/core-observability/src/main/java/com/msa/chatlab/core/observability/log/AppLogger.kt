package com.msa.chatlab.core.observability.log

interface AppLogger {
    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        context: Map<String, String> = emptyMap(),
        throwable: Throwable? = null
    )

    fun d(tag: String, msg: String, ctx: Map<String, String> = emptyMap()) = log(LogLevel.DEBUG, tag, msg, ctx)
    fun i(tag: String, msg: String, ctx: Map<String, String> = emptyMap()) = log(LogLevel.INFO, tag, msg, ctx)
    fun w(tag: String, msg: String, ctx: Map<String, String> = emptyMap(), tr: Throwable? = null) =
        log(LogLevel.WARN, tag, msg, ctx, tr)
    fun e(tag: String, msg: String, ctx: Map<String, String> = emptyMap(), tr: Throwable? = null) =
        log(LogLevel.ERROR, tag, msg, ctx, tr)
}
