package com.msa.chatlab

import android.app.Application
import android.os.StrictMode
import androidx.lifecycle.ProcessLifecycleOwner
import com.msa.chatlab.di.AppModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatLabApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }

        startKoin {
            androidContext(this@ChatLabApp)
            modules(AppModule)
        }

        val lifecycleObserver = get<AppLifecycleObserver>()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }
}
