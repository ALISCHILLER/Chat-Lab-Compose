package com.msa.chatlab.di

import com.msa.chatlab.bootstrap.StartupBootstrapper
import com.msa.chatlab.bootstrap.StartupNoticeStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val StartupModule = module {
    single { StartupNoticeStore(androidContext()) }
    single {
        StartupBootstrapper(
            context = androidContext(),
            profileManager = get(),
            activeStore = get(),
            noticeStore = get()
        )
    }
}
