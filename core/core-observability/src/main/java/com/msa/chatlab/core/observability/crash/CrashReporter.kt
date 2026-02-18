package com.msa.chatlab.core.observability.crash

interface CrashReporter {
    fun breadcrumb(message: String, context: Map<String, String> = emptyMap())
    fun setKey(key: String, value: String)
    fun record(throwable: Throwable, context: Map<String, String> = emptyMap())
}

object NoOpCrashReporter : CrashReporter {
    override fun breadcrumb(message: String, context: Map<String, String>) = Unit
    override fun setKey(key: String, value: String) = Unit
    override fun record(throwable: Throwable, context: Map<String, String>) = Unit
}
