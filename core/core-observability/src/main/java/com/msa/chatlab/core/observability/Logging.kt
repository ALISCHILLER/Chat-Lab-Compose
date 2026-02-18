package com.msa.chatlab.core.observability

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }

data class LogLine(
    val id: Int,
    val ts: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    fun toLine(): String {
        val t = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(ts))
        val th = throwable?.let { "\n${Log.getStackTraceString(it)}" } ?: ""
        return "[$t] ${level.name.padEnd(5)} $tag: $message$th"
    }
}

interface Logger {
    val lines: StateFlow<List<LogLine>>
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
    fun clear()
}

fun Logger.v(tag: String, msg: String) = log(LogLevel.VERBOSE, tag, msg)
fun Logger.d(tag: String, msg: String) = log(LogLevel.DEBUG, tag, msg)
fun Logger.i(tag: String, msg: String) = log(LogLevel.INFO, tag, msg)
fun Logger.w(tag: String, msg: String, t: Throwable? = null) = log(LogLevel.WARN, tag, msg, t)
fun Logger.e(tag: String, msg: String, t: Throwable? = null) = log(LogLevel.ERROR, tag, msg, t)

class RingBufferLogger(
    private val capacity: Int = 800
) : Logger {

    private val _lines = MutableStateFlow<List<LogLine>>(emptyList())
    override val lines: StateFlow<List<LogLine>> = _lines.asStateFlow()

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val line = LogLine(
            id = 0, // This should be unique, but for now...
            ts = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )
        val current = _lines.value
        val next = if (current.size < capacity) {
            current + line
        } else {
            current.drop(current.size - capacity + 1) + line
        }
        _lines.value = next
    }

    override fun clear() {
        _lines.value = emptyList()
    }
}

class CrashReporter(
    private val logger: Logger
) {
    fun install() {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            logger.e("CrashReporter", "Uncaught exception on thread=${t.name}", e)
            prev?.uncaughtException(t, e)
        }
    }
}
