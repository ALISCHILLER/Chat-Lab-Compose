package com.msa.chatlab.di

import org.koin.dsl.module

val AppModule = module {
    includes(
        FeatureModule,
        LabModule,
        ConnectionModule,
    )
}