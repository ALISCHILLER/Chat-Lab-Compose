package com.msa.chatlab.core.observability.log

import android.util.Log

class LogcatLogger : AppLogger {
    override fun log(level: LogLevel, tag: String, message: String, context: Map<String, String>, throwable: Throwable?) {
        val ctx = if (context.isEmpty()) "" else " | ctx=$context"
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
