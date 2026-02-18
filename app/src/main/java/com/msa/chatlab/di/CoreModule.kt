package com.msa.chatlab.di

import com.msa.chatlab.core.common.ui.messenger.ChannelUiMessenger
import com.msa.chatlab.core.common.ui.messenger.UiMessenger
import com.msa.chatlab.core.data.di.DataModule
import com.msa.chatlab.core.observability.di.ObservabilityModule
import com.msa.chatlab.core.storage.di.StorageModule
import org.koin.dsl.module

val CoreModule = module {
    includes(
        ObservabilityModule,
        DataModule,
        StorageModule
    )

    single<UiMessenger> { ChannelUiMessenger() }
}
