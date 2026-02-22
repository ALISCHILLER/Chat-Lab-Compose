package com.msa.chatlab.di

import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import org.koin.dsl.module

// Includes all the real, existing Koin modules for the app.
val AppModule = module {
    includes(
        DataModule,
        FeatureModule,

        // Only WsOkHttp provides a Koin module. The others are not implemented.
        WsOkHttpProtocolModule
    )
}
