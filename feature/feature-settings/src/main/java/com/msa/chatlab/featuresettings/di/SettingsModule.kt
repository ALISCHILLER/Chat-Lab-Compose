package com.msa.chatlab.featuresettings.di

import com.msa.chatlab.featuresettings.vm.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            profileManager = get(),
            activeStore = get(),
            codec = get()
        )
    }
}
