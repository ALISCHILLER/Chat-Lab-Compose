package com.msa.chatlab.feature.settings.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.msa.chatlab.feature.settings.screen.SettingsScreen
import com.msa.chatlab.feature.settings.vm.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onGoLab: () -> Unit,
    onGoConnect: () -> Unit,
    onGoChat: () -> Unit,
    onGoDebug: () -> Unit
) {
    val vm: SettingsViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    SettingsScreen(
        state = state,
        onEvent = vm::onEvent
    )
}
