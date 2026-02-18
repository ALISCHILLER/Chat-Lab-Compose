package com.msa.chatlab.feature.connect.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.msa.chatlab.feature.connect.screen.ConnectScreen
import com.msa.chatlab.feature.connect.state.ConnectUiEffect
import com.msa.chatlab.feature.connect.vm.ConnectViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectRoute(
    padding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onGoSettings: () -> Unit
) {
    val vm: ConnectViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                is ConnectUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = eff.error.message,
                        actionLabel = "Details"
                    )
                }
            }
        }
    }

    ConnectScreen(
        state = state,
        onEvent = vm::onEvent,
        padding = padding,
        onGoSettings = onGoSettings
    )
}
