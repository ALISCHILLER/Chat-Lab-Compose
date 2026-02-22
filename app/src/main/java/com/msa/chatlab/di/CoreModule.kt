package com.msa.chatlab.di

import com.msa.chatlab.core.common.concurrency.AppScope
import com.msa.chatlab.core.common.concurrency.DefaultAppScope
import com.msa.chatlab.core.common.ui.ChannelUiMessenger
import com.msa.chatlab.core.common.ui.UiMessenger

import com.msa.chatlab.core.observability.di.ObservabilityModule
import com.msa.chatlab.core.storage.di.StorageModule
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val CoreModule = module {
    includes(
        ObservabilityModule,
        StorageModule
    )

    single<UiMessenger> { ChannelUiMessenger() }

    single<AppScope> { DefaultAppScope(coroutineDispatcher = Dispatchers.IO) }
}
