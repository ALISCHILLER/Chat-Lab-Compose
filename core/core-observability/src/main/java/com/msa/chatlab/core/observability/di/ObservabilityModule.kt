package com.msa.chatlab.core.observability.di

import com.msa.chatlab.core.observability.Logger
import com.msa.chatlab.core.observability.RingBufferLogger
import org.koin.dsl.module

val ObservabilityModule = module {
    single<Logger> { RingBufferLogger(capacity = 1200) }
}
