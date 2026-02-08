package com.msa.chatlab

import android.app.Application
import com.msa.chatlab.di.ConnectionModule
import com.msa.chatlab.di.DataModule
import com.msa.chatlab.di.FeatureModule
import com.msa.chatlab.di.StorageModule
import com.msa.chatlab.di.appModule
import com.msa.chatlab.di.coreModule
import com.msa.chatlab.protocol.websocket.okhttp.di.WsOkHttpProtocolModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class ChatLabApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatLabApp)
            modules(
                appModule,
                coreModule,
                DataModule,
                StorageModule,
                ConnectionModule,
                FeatureModule,
                WsOkHttpProtocolModule
            )
        }

        // Start the OutboxProcessor
        val koin = GlobalContext.get().koin
        koin.get<com.msa.chatlab.core.data.outbox.OutboxProcessor>().start()
    }
}
