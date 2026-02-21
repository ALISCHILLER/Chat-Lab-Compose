package com.msa.chatlab.di

import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import org.koin.dsl.module

val AppModule = module {
    includes(
        CoreModule,
        DataModule,
        FeatureModule,

        WsOkHttpProtocolModule,
        // KtorProtocolModule,
        // MqttProtocolModule,
        // SocketIoProtocolModule,
        // SignalRProtocolModule,
    )
}
