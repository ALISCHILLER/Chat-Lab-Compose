package com.msa.chatlab.di

import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import org.koin.dsl.module

val AppModule = module {
    includes(
        FeatureModule,

        // Protocol Modules (هرکدام Binding می‌دهند)
        WsOkHttpProtocolModule,
        // KtorProtocolModule,
        // MqttProtocolModule,
        // SocketIoProtocolModule,
        // SignalRProtocolModule,
    )
}
