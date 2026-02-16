package com.msa.chatlab.feature.chat.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.chatlab.feature.chat.screen.ChatScreen
import com.msa.chatlab.feature.chat.vm.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatRoute(padding: PaddingValues) {
    val vm: ChatViewModel = koinViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    var input by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("default") }

    ChatScreen(
        state = state,
        padding = padding,
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
