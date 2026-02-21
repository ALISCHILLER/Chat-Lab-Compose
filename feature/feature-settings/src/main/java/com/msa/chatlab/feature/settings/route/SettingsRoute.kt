package com.msa.chatlab.feature.settings.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.msa.chatlab.core.common.ui.UiMessenger
import com.msa.chatlab.feature.settings.screen.SettingsScreen
import com.msa.chatlab.feature.settings.state.SettingsUiEffect
import com.msa.chatlab.feature.settings.vm.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    padding: PaddingValues,
) {
    val vm: SettingsViewModel = koinViewModel()
    val messenger: UiMessenger = get()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                is SettingsUiEffect.Toast ->
                    messenger.trySnackbar(eff.message)
            }
        }
    }

    SettingsScreen(
        padding = padding,
        state = state,
        onEvent = vm::onEvent
    )
}