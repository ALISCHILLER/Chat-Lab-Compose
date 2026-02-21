package com.msa.chatlab.feature.debug.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.debug.vm.DebugUiState

@Composable
fun DebugScreen(
    padding: PaddingValues,
    state: DebugUiState,
    onToggleOffline: (Boolean) -> Unit,
    onClearLogs: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Debug", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Active profile: ${state.activeProfileName}")
                Text("Connection: ${state.connection}")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Switch(checked = state.simulateOffline, onCheckedChange = onToggleOffline)
                    Text("Simulate offline")
                }
                Text("Connection events: ${state.connectionEvents}")
                Button(onClick = onClearLogs) { Text("Clear logs") }
            }
        }

        Text("App Logs (last 200)", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.logLines) { logLine ->
                Text(logLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}