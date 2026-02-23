package com.msa.chatlab.di

import com.msa.chatlab.core.common.concurrency.AppScope
import com.msa.chatlab.core.common.concurrency.DefaultAppScope
import com.msa.chatlab.core.common.concurrency.DefaultDispatcherProvider
import com.msa.chatlab.core.common.concurrency.DispatcherProvider
import com.msa.chatlab.core.common.ui.ChannelUiMessenger
import com.msa.chatlab.core.common.ui.UiMessenger
import com.msa.chatlab.core.observability.di.ObservabilityModule
import com.msa.chatlab.core.storage.di.StorageModule
import org.koin.dsl.module

val CoreModule = module {
    includes(
        ObservabilityModule,
        StorageModule
    )

    single<UiMessenger> { ChannelUiMessenger() }

    // Provide a single DispatcherProvider for the whole app
    single<DispatcherProvider> { DefaultDispatcherProvider() }

    // Provide a single AppScope for the whole app
    single<AppScope> { DefaultAppScope(get()) }
}
