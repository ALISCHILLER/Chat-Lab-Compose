package com.msa.chatlab

import android.app.Application
import com.msa.chatlab.di.AppModule
import com.msa.chatlab.di.CoreModule
// import com.msa.chatlab.protocol.signalr.di.SignalRProtocolModule
// import com.msa.chatlab.protocol.socketio.di.SocketIoProtocolModule
// import com.msa.chatlab.protocol.websocket.ktor.di.KtorProtocolModule
// import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class ChatLabApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatLabApp)
            modules(
                AppModule,
                CoreModule,
                // WsOkHttpProtocolModule,
                // KtorProtocolModule,
                // SocketIoProtocolModule,
                // SignalRProtocolModule
            )
        }

        // Start the OutboxProcessor
        // val koin = GlobalContext.get().koin
        // koin.get<com.msa.chatlab.core.data.outbox.OutboxProcessor>().start()
    }
}
