package com.msa.chatlab.feature.settings.component.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.MqttConfig

@Composable
fun MqttEditor(
    config: MqttConfig,
    onConfigChanged: (MqttConfig) -> Unit
) {
    var endpoint by remember(config.endpoint) { mutableStateOf(config.endpoint) }
    var clientId by remember(config.clientId) { mutableStateOf(config.clientId) }
    var topic by remember(config.topic) { mutableStateOf(config.topic) }
    var qos by remember(config.qos) { mutableStateOf(config.qos.toString()) }
    var cleanSession by remember(config.cleanSession) { mutableStateOf(config.cleanSession) }
    var username by remember(config.username) { mutableStateOf(config.username.orEmpty()) }
    var password by remember(config.password) { mutableStateOf(config.password.orEmpty()) }

    LaunchedEffect(endpoint, clientId, topic, qos, cleanSession, username, password) {
        onConfigChanged(
            config.copy(
                endpoint = endpoint,
                clientId = clientId,
                topic = topic,
                qos = qos.toIntOrNull() ?: 1,
                cleanSession = cleanSession,
                username = username.ifBlank { null },
                password = password.ifBlank { null }
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("MQTT Configuration", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text("Broker endpoint *") },
            placeholder = { Text("tcp://broker.hivemq.com:1883") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID *") },
            placeholder = { Text("chatlab-device-123") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            label = { Text("Topic *") },
            placeholder = { Text("chatlab/messages") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = qos,
                onValueChange = { qos = it.filter { c -> c.isDigit() && c.digitToInt() in 0..2 } },
                label = { Text("QoS (0/1/2)") },
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = cleanSession,
                    onCheckedChange = { cleanSession = it },
                )
                Text("Clean session")
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (optional)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
