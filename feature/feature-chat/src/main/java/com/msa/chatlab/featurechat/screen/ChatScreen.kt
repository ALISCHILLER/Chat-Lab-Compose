package com.msa.chatlab.featurechat.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.featurechat.model.ChatMessageUi
import com.msa.chatlab.featurechat.state.ChatUiState

@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleSimOffline: () -> Unit,
    onClearError: () -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Chat", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Active: ${state.activeProfileName}")
                Text("State: ${state.connectionState.pretty()}")

                // ✅ Outbox counter
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📤 Outbox: ${state.outboxCount}", style = MaterialTheme.typography.titleMedium)
                    AssistChip(
                        onClick = onToggleSimOffline,
                        label = { Text(if (state.simulateOffline) "Sim Offline: ON" else "Sim Offline: OFF") }
                    )
                }

                state.lastEvent?.let { Text("Last: $it") }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(state.messages) { m ->
                MessageBubble(m)
            }
        }

        state.error?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(err, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onClearError) { Text("Clear") }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                label = { Text("Message") }
            )
            Button(onClick = onSend) { Text("Send") }
        }
    }
}

@Composable
private fun MessageBubble(m: ChatMessageUi) {
    val title = if (m.direction == ChatMessageUi.Direction.OUT) "You" else "Server"

    val colors = if (m.queued && m.direction == ChatMessageUi.Direction.OUT) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    } else {
        CardDefaults.cardColors()
    }

    Card(colors = colors) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (m.queued) Text("Queued", style = MaterialTheme.typography.labelMedium)
            }
            Text(m.text)
        }
    }
}

private fun ConnectionState.pretty(): String = when (this) {
    is ConnectionState.Idle -> "Idle"
    is ConnectionState.Connecting -> "Connecting..."
    is ConnectionState.Connected -> "Connected"
    is ConnectionState.Disconnected -> "Disconnected (${reason ?: "unknown"})"
}
