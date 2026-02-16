package com.msa.chatlab.di

import com.msa.chatlab.core.data.repository.ProtocolRegistry
import com.msa.chatlab.protocol.mqtt.MqttTransport
import com.msa.chatlab.protocol.signalr.SignalRTransport
import com.msa.chatlab.protocol.socketio.SocketIoTransport
import com.msa.chatlab.protocol.ws.ktor.KtorTransport
import com.msa.chatlab.protocol.ws.okhttp.WsOkHttpTransport
import org.koin.dsl.module

val connectionModule = module {
    single {
        ProtocolRegistry(listOf(
            WsOkHttpTransport(get()),
            KtorTransport(get()),
            MqttTransport(get()),
            SocketIoTransport(get()),
            SignalRTransport(get())
        ))
    }
}
