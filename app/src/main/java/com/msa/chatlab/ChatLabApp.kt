package com.msa.chatlab

import android.app.Application
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.di.AppModule
import com.msa.chatlab.di.CoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

class ChatLabApp : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatLabApp)
            modules(
                AppModule,
                CoreModule,
            )
        }

        // Start the OutboxProcessor
        get<OutboxProcessor>().start()
    }
}
