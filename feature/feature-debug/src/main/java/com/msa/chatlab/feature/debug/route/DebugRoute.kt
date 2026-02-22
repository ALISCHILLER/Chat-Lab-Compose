package com.msa.chatlab.feature.debug.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.msa.chatlab.feature.debug.screen.DebugScreen
import com.msa.chatlab.feature.debug.vm.DebugViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DebugRoute(padding: PaddingValues) {
    val vm: DebugViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    DebugScreen(
        padding = padding,
        state = state,
        onToggleOffline = vm::setSimOffline,
        onClearLogs = vm::clearLogs
    )
}
