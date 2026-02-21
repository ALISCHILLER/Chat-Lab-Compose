package com.msa.chatlab.di

import com.msa.chatlab.protocol.mqtt.di.MqttProtocolModule
import com.msa.chatlab.protocol.signalr.di.SignalRProtocolModule
import com.msa.chatlab.protocol.socketio.di.SocketIoProtocolModule
import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import com.msa.chatlab.protocol.ws.ktor.di.WsKtorProtocolModule
import org.koin.dsl.module

val AppModule = module {
    includes(
        CoreModule,
        DataModule,
        FeatureModule,

        WsOkHttpProtocolModule,
        WsKtorProtocolModule,
        MqttProtocolModule,
        SocketIoProtocolModule,
        SignalRProtocolModule,
    )
}