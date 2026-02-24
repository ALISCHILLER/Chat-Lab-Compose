package com.msa.chatlab.feature.chat.di

import com.msa.chatlab.feature.chat.vm.ChatListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureChatModule = module {
    viewModel { ChatListViewModel(get(), get()) }
}
