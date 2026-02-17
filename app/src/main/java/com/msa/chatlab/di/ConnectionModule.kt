package com.msa.chatlab.di

import android.content.Context
import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.protocol.mqtt.MqttTransport
import com.msa.chatlab.protocol.signalr.SignalRTransport
import com.msa.chatlab.protocol.socketio.SocketIoTransport
import com.msa.chatlab.protocol.ws.ktor.KtorTransport
import com.msa.chatlab.protocol.websocket.okhttp.transport.WsOkHttpTransport
import org.koin.dsl.module

val ConnectionModule = module {
    single {
        val context = get<Context>()
        val bindings = listOf(
            object : ProtocolBinding {
                override val type = ProtocolType.WS_OKHTTP
                override fun create(profile: Profile) = WsOkHttpTransport(profile)
            },
            object : ProtocolBinding {
                override val type = ProtocolType.WS_KTOR
                override fun create(profile: Profile) = KtorTransport(context)
            },
            object : ProtocolBinding {
                override val type = ProtocolType.MQTT
                override fun create(profile: Profile) = MqttTransport(context)
            },
            object : ProtocolBinding {
                override val type = ProtocolType.SOCKETIO
                override fun create(profile: Profile) = SocketIoTransport(context)
            },
            object : ProtocolBinding {
                override val type = ProtocolType.SIGNALR
                override fun create(profile: Profile) = SignalRTransport(context)
            }
        )
        ProtocolRegistry(bindings)
    }
}
