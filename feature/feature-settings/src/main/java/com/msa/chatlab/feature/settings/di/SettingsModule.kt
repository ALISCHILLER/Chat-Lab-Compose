package com.msa.chatlab.feature.settings.di

import com.msa.chatlab.feature.settings.vm.SettingsViewModel
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
