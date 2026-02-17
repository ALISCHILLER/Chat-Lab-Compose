package com.msa.chatlab.feature.lab.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.msa.chatlab.feature.lab.screen.LabScreen
import com.msa.chatlab.feature.lab.vm.LabViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LabRoute(padding: PaddingValues) {
    val vm: LabViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    LabScreen(
        state = state,
        onEvent = vm::onEvent,
        padding = padding
    )
}
