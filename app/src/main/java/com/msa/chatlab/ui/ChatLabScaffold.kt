package com.msa.chatlab.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable

@Composable
fun ChatLabScaffold(
    appState: ChatLabAppState,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = { if (appState.shouldShowBottomBar) AppBottomBar(appState) },
        snackbarHost = { SnackbarHost(hostState = appState.snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        content(padding)
    }
}
