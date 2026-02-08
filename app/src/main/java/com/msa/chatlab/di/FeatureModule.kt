package com.msa.chatlab.di

import com.msa.chatlab.featurechat.vm.ChatViewModel
import com.msa.chatlab.featureconnect.vm.ConnectViewModel
import com.msa.chatlab.featuresettings.vm.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureModule = module {
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { ConnectViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
}
