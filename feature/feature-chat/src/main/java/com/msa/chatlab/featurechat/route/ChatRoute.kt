package com.msa.chatlab.featurechat.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.msa.chatlab.featurechat.screen.ChatScreen
import com.msa.chatlab.featurechat.vm.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatRoute(padding: PaddingValues) {
    val vm: ChatViewModel = koinViewModel()
    val state = vm.uiState.collectAsState()

    ChatScreen(
        state = state.value,
        onInputChange = vm::onInputChange,
        onSend = vm::send,
        onToggleSimOffline = vm::toggleSimulateOffline,
        onClearError = vm::clearError,
        padding = padding
    )
}
