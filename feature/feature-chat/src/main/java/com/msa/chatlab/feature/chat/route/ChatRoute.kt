package com.msa.chatlab.feature.chat.route

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msa.chatlab.feature.chat.screen.ChatScreen
import com.msa.chatlab.feature.chat.vm.ChatViewModel

@Composable
fun ChatRoute() {
    val vm: ChatViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    var input by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("default") }

    ChatScreen(
        state = state,
        input = input,
        destination = destination,
        onInputChange = { input = it },
        onDestinationChange = { destination = it },
        onSend = {
            vm.send(input, destination)
            input = ""
        },
        onClearError = vm::clearError
    )
}
