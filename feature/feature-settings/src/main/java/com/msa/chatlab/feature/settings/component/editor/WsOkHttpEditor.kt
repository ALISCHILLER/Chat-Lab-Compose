package com.msa.chatlab.feature.settings.component.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.WsOkHttpConfig

@Composable
fun WsOkHttpEditor(
    config: WsOkHttpConfig,
    onConfigChanged: (WsOkHttpConfig) -> Unit
) {
    var endpoint by remember(config.endpoint) { mutableStateOf(config.endpoint) }
    var pingInterval by remember(config.pingIntervalMs) { mutableStateOf(config.pingIntervalMs.toString()) }
    var headers by remember(config.headers) { mutableStateOf(config.headers.toList()) }
    var newHeaderKey by remember { mutableStateOf("") }
    var newHeaderValue by remember { mutableStateOf("") }

    LaunchedEffect(endpoint, pingInterval, headers) {
        onConfigChanged(
            config.copy(
                endpoint = endpoint,
                pingIntervalMs = pingInterval.toLongOrNull() ?: 15000,
                headers = headers.toMap()
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("WebSocket (OkHttp) Configuration", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text("Endpoint (wss://...) *") },
            placeholder = { Text("wss://echo.websocket.events") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pingInterval,
            onValueChange = { pingInterval = it.filter { c -> c.isDigit() } },
            label = { Text("Ping interval (ms)") },
            placeholder = { Text("15000") },
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text("Headers (optional)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newHeaderKey,
                    onValueChange = { newHeaderKey = it },
                    placeholder = { Text("Header name") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = newHeaderValue,
                    onValueChange = { newHeaderValue = it },
                    placeholder = { Text("Value") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newHeaderKey.isNotBlank() && newHeaderValue.isNotBlank()) {
                            headers = headers + (newHeaderKey to newHeaderValue)
                            newHeaderKey = ""
                            newHeaderValue = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add header")
                }
            }

            headers.forEachIndexed { index, (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$key: $value", style = MaterialTheme.typography.bodySmall)
                    IconButton(
                        onClick = {
                            headers = headers.filterIndexed { i, _ -> i != index }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                    }
                }
            }
        }
    }
}
