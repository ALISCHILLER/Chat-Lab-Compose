package com.msa.chatlab.core.observability.log

/**
 * همزمان هم LogStore رو پر می‌کند هم می‌تونه به Logcat delegate بده.
 */
class StoreLogger(
    private val store: LogStore,
    private val delegate: AppLogger? = null
) : AppLogger {

    override fun log(level: LogLevel, tag: String, message: String, context: Map<String, String>, throwable: Throwable?) {
        store.append(
            LogEntry(
                ts = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message,
                context = context,
                throwable = throwable
            )
        )
        delegate?.log(level, tag, message, context, throwable)
    }
}
