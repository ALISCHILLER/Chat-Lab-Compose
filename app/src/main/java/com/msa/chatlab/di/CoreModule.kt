package com.msa.chatlab.di

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
}
