package com.msa.chatlab.feature.chat.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.chatlab.core.common.ui.insets.AppImeInsets
import com.msa.chatlab.feature.chat.vm.ChatThreadViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatThreadRoute(
    padding: PaddingValues,
    destination: String,
    onBack: () -> Unit
) {
    val vm: ChatThreadViewModel = koinViewModel()
    val st = vm.state(destination).collectAsStateWithLifecycle().value

    ChatThreadScreen(
        padding = padding,
        destination = destination,
        state = st,
        onBack = onBack,
        onSend = { text -> vm.send(destination, text) },
        imeInsets = AppImeInsets
    )
}
