package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.settings.state.SettingsUiEvent
import com.msa.chatlab.feature.settings.state.SettingsUiState
import com.msa.chatlab.feature.settings.component.common.ProfileCard

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    padding: PaddingValues,
    onEvent: (SettingsUiEvent) -> Unit,
    onGoLab: () -> Unit,
    onGoConnect: () -> Unit,
    onGoChat: () -> Unit,
    onGoDebug: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onGoConnect) { Text("Go to Connect") }
        Button(onClick = onGoChat) { Text("Go to Chat") }
        Button(onClick = onGoLab) { Text("Go to Lab") }
        Button(onClick = onGoDebug) { Text("Go to Debug") }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.cards) { card ->
                ProfileCard(profile = card, onEvent = onEvent)
            }
        }
    }
}
