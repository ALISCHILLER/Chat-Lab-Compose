package com.msa.chatlab.featureconnect.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.featureconnect.state.ConnectUiEvent
import com.msa.chatlab.featureconnect.state.ConnectUiState

@Composable
fun ConnectScreen(
    state: ConnectUiState,
    onEvent: (ConnectUiEvent) -> Unit,
    padding: PaddingValues,
    onGoSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Connect", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Connection State: ${state.connectionState.asLabel()}")

                if (state.activeProfile == null) {
                    Text("No active profile.", color = MaterialTheme.colorScheme.error)
                    Button(onClick = onGoSettings) { Text("Go to Settings") }
                } else {
                    Text("Active Profile: ${state.activeProfile.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Protocol: ${state.activeProfile.protocol}")
                    Text("Endpoint: ${state.activeProfile.endpoint}")
                }

                state.lastError?.let {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { onEvent(ConnectUiEvent.ClearError) }) { Text("Clear error") }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onEvent(ConnectUiEvent.Prepare) },
                enabled = state.activeProfile != null
            ) { Text("Prepare") }

            Button(
                onClick = { onEvent(ConnectUiEvent.Connect) },
                enabled = state.activeProfile != null && state.connectionState !is ConnectionState.Connected
            ) { Text("Connect") }

            OutlinedButton(onClick = { onEvent(ConnectUiEvent.Disconnect) }) { Text("Disconnect") }
        }
    }
}

private fun ConnectionState.asLabel(): String = when (this) {
    is ConnectionState.Idle -> "Idle"
    is ConnectionState.Connecting -> "Connecting..."
    is ConnectionState.Connected -> "Connected"
    is ConnectionState.Disconnected -> "Disconnected (${this.reason ?: "unknown"})"
}
