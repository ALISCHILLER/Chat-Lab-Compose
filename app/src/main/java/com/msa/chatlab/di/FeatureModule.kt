package com.msa.chatlab.di

import com.msa.chatlab.feature.chat.vm.ChatViewModel
import com.msa.chatlab.feature.connect.vm.ConnectViewModel
import com.msa.chatlab.feature.settings.vm.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureModule = module {
    viewModel { ConnectViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
}
