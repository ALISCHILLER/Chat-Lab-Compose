package com.msa.chatlab.feature.connect.route

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.msa.chatlab.feature.connect.screen.ConnectScreen
import com.msa.chatlab.feature.connect.state.ConnectUiEffect
import com.msa.chatlab.feature.connect.vm.ConnectViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectRoute(
    padding: PaddingValues
) {
    val vm: ConnectViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                is ConnectUiEffect.Toast -> Toast.makeText(ctx, eff.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ConnectScreen(
        state = state,
        onEvent = vm::onEvent,
        padding = padding
    )
}
