package com.msa.chatlab.di

import com.msa.chatlab.BuildConfig
import com.msa.chatlab.protocol.signalr.di.SignalRProtocolModule
import com.msa.chatlab.protocol.socketio.di.SocketIoProtocolModule
import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Includes all the real, existing Koin modules for the app.
val AppModule = module {
    includes(
        DataModule,
        FeatureModule,

        // Protocol implementations
        WsOkHttpProtocolModule,
        SignalRProtocolModule,
        SocketIoProtocolModule
    )

    // Provide the actual min log level from the app's BuildConfig.
    // This overrides the default value provided in ObservabilityModule because it's loaded last.
    single(named("minLogLevel")) { BuildConfig.MIN_LOG_LEVEL }
}
