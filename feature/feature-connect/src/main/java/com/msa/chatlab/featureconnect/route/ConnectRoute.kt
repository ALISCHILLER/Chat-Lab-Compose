package com.msa.chatlab.featureconnect.route

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.msa.chatlab.featureconnect.screen.ConnectScreen
import com.msa.chatlab.featureconnect.state.ConnectUiEffect
import com.msa.chatlab.featureconnect.vm.ConnectViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectRoute(
    padding: PaddingValues,
    onGoSettings: () -> Unit
) {
    val vm: ConnectViewModel = koinViewModel()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                is ConnectUiEffect.Toast -> Toast.makeText(ctx, eff.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ConnectScreen(
        state = vm.state.value,
        onEvent = vm::onEvent,
        padding = padding,
        onGoSettings = onGoSettings
    )
}
