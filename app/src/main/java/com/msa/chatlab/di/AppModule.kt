package com.msa.chatlab.di
import com.msa.chatlab.BuildConfig
import com.msa.chatlab.protocol.mqtt.di.MqttProtocolModule
import com.msa.chatlab.protocol.signalr.di.signalRProtocolModule
import com.msa.chatlab.protocol.socketio.di.SocketIoProtocolModule
import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import com.msa.chatlab.protocol.ws.ktor.di.KtorWsProtocolModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Includes all the real, existing Koin modules for the app.
val AppModule = module {
    includes(
        DataModule,
        FeatureModule,

        // Protocol implementations
        WsOkHttpProtocolModule,
        KtorWsProtocolModule,
        MqttProtocolModule,
        signalRProtocolModule,
        SocketIoProtocolModule
    )

    // Provide the actual min log level from the app's BuildConfig.
    // This overrides the default value provided in ObservabilityModule because it's loaded last.
    single(named("minLogLevel")) { BuildConfig.MIN_LOG_LEVEL }
}
