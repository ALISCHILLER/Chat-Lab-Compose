package com.msa.chatlab.core.observability

interface AppLogger {
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

interface CrashReporter {
    fun report(throwable: Throwable, message: String? = null)
}
