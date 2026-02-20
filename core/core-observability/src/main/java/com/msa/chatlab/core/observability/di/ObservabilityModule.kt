package com.msa.chatlab.core.observability.di

import com.msa.chatlab.core.observability.crash.CrashReporter
import com.msa.chatlab.core.observability.crash.NoOpCrashReporter
import com.msa.chatlab.core.observability.log.*
import org.koin.dsl.module

val ObservabilityModule = module {

    // Log store
    single<LogStore> { InMemoryLogStore(capacity = 2000) }

    // App logger: هم تو LogStore ذخیره می‌کنه هم Logcat
    single<AppLogger> { StoreLogger(get(), LogcatLogger()) }

    // CrashReporter (فعلاً NoOp تا فازهای بعدی)
    single<CrashReporter> { NoOpCrashReporter }
}
