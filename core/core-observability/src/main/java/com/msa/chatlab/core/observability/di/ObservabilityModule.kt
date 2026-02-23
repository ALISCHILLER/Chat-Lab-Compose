package com.msa.chatlab.core.observability.di

import com.msa.chatlab.core.observability.crash.CrashReporter
import com.msa.chatlab.core.observability.crash.NoOpCrashReporter
import com.msa.chatlab.core.observability.log.AppLogger
import com.msa.chatlab.core.observability.log.InMemoryLogStore
import com.msa.chatlab.core.observability.log.LogStore
import com.msa.chatlab.core.observability.log.LogcatLogger
import com.msa.chatlab.core.observability.log.StoreLogger
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ObservabilityModule = module {

    // The minimum log level, provided by the app module from BuildConfig.
    // Using a named qualifier to avoid type conflicts.
    single(named("minLogLevel")) { "DEBUG" } // Default value

    // Log store
    single<LogStore> { InMemoryLogStore(capacity = 2000) }

    // App logger: composites a store logger and a logcat logger.
    single<AppLogger> {
        val minLogLevel: String = get(named("minLogLevel"))
        StoreLogger(get(), LogcatLogger(minLogLevel))
    }

    // CrashReporter (currently a NoOp until a real one is added).
    single<CrashReporter> { NoOpCrashReporter }
}
