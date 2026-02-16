package com.msa.chatlab.featuresettings.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.msa.chatlab.featuresettings.screen.SettingsScreen
import com.msa.chatlab.featuresettings.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    padding: PaddingValues,
    onGoLab: () -> Unit,
    onGoConnect: () -> Unit,
    onGoChat: () -> Unit,
    onGoDebug: () -> Unit
) {
    val vm: SettingsViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    SettingsScreen(
        state = state,
        padding = padding,
        onEvent = vm::onEvent,
        onGoLab = onGoLab,
        onGoConnect = onGoConnect,
        onGoChat = onGoChat,
        onGoDebug = onGoDebug
    )
}
