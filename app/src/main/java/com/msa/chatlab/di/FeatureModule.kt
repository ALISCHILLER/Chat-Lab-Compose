package com.msa.chatlab.di

import com.msa.chatlab.feature.chat.vm.ChatListViewModel
import com.msa.chatlab.feature.chat.vm.ChatThreadViewModel
import com.msa.chatlab.feature.chat.vm.OutboxViewModel
import com.msa.chatlab.feature.connect.vm.ConnectViewModel
import com.msa.chatlab.feature.lab.vm.LabViewModel
import com.msa.chatlab.feature.settings.vm.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureModule = module {
    viewModel { ChatListViewModel(get(), get()) }
    viewModel { ChatThreadViewModel(get(), get(), get()) }
    viewModel { OutboxViewModel(get(), get()) }
    viewModel { ConnectViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) } // ✅ +ProtocolRegistry
    viewModel { LabViewModel(get(), get()) }
}