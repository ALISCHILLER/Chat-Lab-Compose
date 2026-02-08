package com.msa.chatlab

import android.app.Application
import com.msa.chatlab.di.appModule
import com.msa.chatlab.di.coreModule
import com.msa.chatlab.di.featureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatLabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ChatLabApp)
            modules(appModule, coreModule, featureModule)
        }
    }
}
