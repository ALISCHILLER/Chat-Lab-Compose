package com.msa.chatlab.di

import com.msa.chatlab.feature.chat.domain.usecase.GetChatUiStateUseCase
import com.msa.chatlab.feature.chat.domain.usecase.SendMessageUseCase
import com.msa.chatlab.feature.chat.vm.ChatViewModel
// import com.msa.chatlab.feature.chat.vm.ChatListViewModel
// import com.msa.chatlab.feature.chat.vm.ChatThreadViewModel
// import com.msa.chatlab.feature.chat.vm.OutboxViewModel
// import com.msa.chatlab.feature.connect.vm.ConnectViewModel
// import com.msa.chatlab.feature.debug.vm.DebugViewModel
// import com.msa.chatlab.feature.lab.vm.LabViewModel
// import com.msa.chatlab.feature.settings.vm.SettingsViewModel
// import com.msa.chatlab.vm.SnackbarViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureModule = module {
    // --- Chat Feature ---
    factory { GetChatUiStateUseCase(get(), get(), get()) }
    factory { SendMessageUseCase(get()) }
    viewModel { ChatViewModel(get(), get(), get()) }

    // --- Other Features (commented out as in original) ---
    // viewModel { ChatListViewModel(get(), get()) }
    // viewModel { ChatThreadViewModel(get(), get(), get()) }
    // viewModel { OutboxViewModel(get(), get()) }
    // viewModel { ConnectViewModel(get(), get(), get()) }
    // viewModel { SettingsViewModel(get(), get(), get(), get()) }
    // viewModel { LabViewModel(get(), get()) }
    // viewModel { DebugViewModel(get(), get(), get(), get()) }
    // viewModel { SnackbarViewModel() }
}
