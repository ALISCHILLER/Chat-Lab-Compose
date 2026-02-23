package com.msa.chatlab.core.observability.log

import android.util.Log

class LogcatLogger(minLogLevelStr: String) : AppLogger {

    private val minLogLevel: LogLevel = try {
        LogLevel.valueOf(minLogLevelStr)
    } catch (e: Exception) {
        LogLevel.DEBUG // Default to DEBUG if parsing fails
    }

    override fun log(level: LogLevel, tag: String, message: String, context: Map<String, String>, throwable: Throwable?) {
        // Only log if the level is equal to or higher than the minimum configured level.
        if (level.ordinal < minLogLevel.ordinal) {
            return
        }

        val maskedContext = mask(context)
        val ctx = if (maskedContext.isEmpty()) "" else " | ctx=$maskedContext"
        val msg = message + ctx

        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, msg, throwable)
            LogLevel.DEBUG -> Log.d(tag, msg, throwable)
            LogLevel.INFO -> Log.i(tag, msg, throwable)
            LogLevel.WARN -> Log.w(tag, msg, throwable)
            LogLevel.ERROR -> Log.e(tag, msg, throwable)
        }
    }
}
