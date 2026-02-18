package com.msa.chatlab.feature.connect.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.connect.R
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.feature.connect.state.ConnectUiEvent
import com.msa.chatlab.feature.connect.state.ConnectUiState

@Composable
fun ConnectScreen(
    state: ConnectUiState,
    onEvent: (ConnectUiEvent) -> Unit,
    padding: PaddingValues,
    onGoSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Profile Card
        if (state.activeProfile == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.connect_no_active_profile), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onGoSettings) { Text(stringResource(R.string.connect_go_to_settings)) }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.activeProfile.name, style = MaterialTheme.typography.titleLarge)
                    Text("${state.activeProfile.protocol} • ${state.activeProfile.endpoint}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(ConnectUiEvent.Prepare) }, enabled = state.activeProfile != null) { Text(stringResource(R.string.connect_prepare)) }
            Button(onClick = { onEvent(ConnectUiEvent.Connect) }, enabled = state.activeProfile != null && state.connectionState !is ConnectionState.Connected) { Text(stringResource(R.string.connect)) }
            OutlinedButton(onClick = { onEvent(ConnectUiEvent.Disconnect) }) { Text(stringResource(R.string.connect_disconnect)) }
        }

        // Stats and Status
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatTile(title = stringResource(R.string.connect_status), value = state.connectionState.asLabel())
            StatTile(title = stringResource(R.string.connect_sent), value = "${state.stats.bytesSent} B")
            StatTile(title = stringResource(R.string.connect_received), value = "${state.stats.bytesReceived} B")
        }

        // Event Log
        Text(stringResource(R.string.connect_event_log), style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.logs) { event ->
                EventLogItem(event)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun StatTile(title: String, value: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EventLogItem(event: TransportEvent) {
    val (icon, text) = when (event) {
        is TransportEvent.Connected -> Icons.Default.Info to "Connected"
        is TransportEvent.Disconnected -> Icons.Default.Info to "Disconnected: ${event.reason}"
        is TransportEvent.MessageReceived -> Icons.Default.Info to "IN: ${event.payload.envelope.messageId.value}"
        is TransportEvent.MessageSent -> Icons.Default.Info to "OUT: ${event.messageId}"
        is TransportEvent.ErrorOccurred -> Icons.Default.Info to "ERROR: ${event.error.message}"
    }
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
    }
}

private fun ConnectionState.asLabel(): String = when (this) {
    is ConnectionState.Idle -> "Idle"
    is ConnectionState.Connecting -> "Connecting..."
    is ConnectionState.Connected -> "Connected"
    is ConnectionState.Disconnected -> "Disconnected"
}
