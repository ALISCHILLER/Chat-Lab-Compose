package com.msa.chatlab.feature.chat.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import com.msa.chatlab.feature.chat.screen.ChatScreen
import com.msa.chatlab.feature.chat.vm.ChatViewModel
import com.msa.chatlab.feature.chat.vm.OutboxViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatRoute(padding: PaddingValues) {
    val chatVm: ChatViewModel = koinViewModel()
    val outboxVm: OutboxViewModel = koinViewModel()

    val chatState by chatVm.uiState.collectAsState()
    val outboxState by outboxVm.uiState.collectAsState()

    var input by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("default") }

    ChatScreen(
        chatState = chatState,
        outboxState = outboxState,
        input = input,
        destination = destination,
        padding = padding,
        onInputChange = { input = it },
        onDestinationChange = { destination = it },
        onSend = {
            chatVm.send(input, destination)
            input = ""
        },
        onClearError = chatVm::clearError,
        onRetryOutbox = outboxVm::onRetryAll,
        onClearOutbox = outboxVm::onClearAll
    )
}
