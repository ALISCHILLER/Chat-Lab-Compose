package com.msa.chatlab.di

import org.koin.dsl.module

val CoreModule = module {
    includes(
        DataModule,
        StorageModule
    )
}