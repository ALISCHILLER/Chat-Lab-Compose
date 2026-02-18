package com.msa.chatlab.di

import com.msa.chatlab.core.observability.di.ObservabilityModule
import com.msa.chatlab.core.storage.di.StorageModule
import org.koin.dsl.module

val CoreModule = module {
    includes(
        ObservabilityModule,
        DataModule,
        StorageModule
    )
}
