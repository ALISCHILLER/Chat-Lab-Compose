package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.CodecMode
import com.msa.chatlab.core.domain.model.MqttConfig
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.model.SignalRConfig
import com.msa.chatlab.core.domain.model.SocketIoConfig
import com.msa.chatlab.core.domain.model.TransportConfig
import com.msa.chatlab.core.domain.model.WsKtorConfig
import com.msa.chatlab.core.domain.model.WsOkHttpConfig
import com.msa.chatlab.feature.settings.component.editor.PayloadChaosEditor
import com.msa.chatlab.feature.settings.state.ImportExportUi
import com.msa.chatlab.feature.settings.state.SettingsUiEvent
import com.msa.chatlab.feature.settings.state.SettingsUiState
import com.msa.chatlab.feature.settings.state.UiProfileCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var menuForId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Connection profiles") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(SettingsUiEvent.NewProfile) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add profile")
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.cards.isEmpty()) {
                Text("No profiles found. Create one with the + button.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.cards) { card ->
                        ProfileCardModern(
                            card = card,
                            onClick = { onEvent(SettingsUiEvent.Apply(card.id)) },
                            onOverflow = { menuForId = card.id }
                        )

                        DropdownMenu(expanded = (menuForId == card.id), onDismissRequest = { menuForId = null }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = {
                                onEvent(SettingsUiEvent.Edit(card.id))
                                menuForId = null
                            })
                            DropdownMenuItem(text = { Text("Duplicate") }, onClick = {
                                onEvent(SettingsUiEvent.Duplicate(card.id))
                                menuForId = null
                            })
                            DropdownMenuItem(text = { Text("Export") }, onClick = {
                                onEvent(SettingsUiEvent.ExportProfile(card.id))
                                menuForId = null
                            })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                onEvent(SettingsUiEvent.RequestDelete(card.id))
                                menuForId = null
                            })
                        }
                    }
                }
            }
        }
    }

    state.pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { onEvent(SettingsUiEvent.DismissDelete) },
            title = { Text("Delete profile?") },
            text = { Text("Are you sure you want to delete this profile? This action is irreversible.") },
            confirmButton = { Button(onClick = { onEvent(SettingsUiEvent.ConfirmDelete) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { onEvent(SettingsUiEvent.DismissDelete) }) { Text("Cancel") } }
        )
    }

    if (state.showImportDialog) {
        ImportExportDialog(
            title = "Import",
            ui = state.importExport,
            onJsonChange = { onEvent(SettingsUiEvent.ImportTextChanged(it)) },
            onConfirmLabel = "Import",
            onConfirm = { onEvent(SettingsUiEvent.ImportCommit) },
            onDismiss = { onEvent(SettingsUiEvent.CloseImport) }
        )
    }

    if (state.showExportDialog) {
        ImportExportDialog(
            title = "Export",
            ui = state.importExport,
            onJsonChange = {},
            readOnly = true,
            onConfirmLabel = "Copy",
            onConfirm = { clipboard.setText(AnnotatedString(state.importExport.json)) },
            onDismiss = { onEvent(SettingsUiEvent.CloseExport) }
        )
    }

    state.editorProfile?.let { profile ->
        ProfileEditorBottomSheet(
            profile = profile,
            supported = state.supportedProtocols,
            availability = state.protocolAvailability,
            validationErrors = state.validationErrors,
            onChange = { onEvent(SettingsUiEvent.EditorChanged(it)) },
            onSave = { onEvent(SettingsUiEvent.EditorSave) },
            onDismiss = { onEvent(SettingsUiEvent.EditorClose) }
        )
    }
}

@Composable
private fun ProfileCardModern(
    card: UiProfileCard,
    onClick: () -> Unit,
    onOverflow: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.title, style = MaterialTheme.typography.titleMedium)
                    if (card.isActive) AssistChip(onClick = {}, label = { Text("Active") })
                }
                Text(card.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onOverflow) { Icon(Icons.Outlined.MoreVert, contentDescription = "More") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditorBottomSheet(
    profile: Profile,
    supported: List<ProtocolType>,
    availability: Map<ProtocolType, Boolean>,
    validationErrors: List<String>,
    onChange: (Profile) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("General", "Protocol", "Reliability", "Payload & Chaos")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit profile", style = MaterialTheme.typography.titleLarge)

            if (validationErrors.isNotEmpty()) {
                AssistChip(
                    onClick = {},
                    label = { Text("Validation: ${validationErrors.first()}") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                )
            }

            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }

            when (tab) {
                0 -> EditorGeneral(profile, onChange)
                1 -> EditorProtocol(profile, supported, availability, onChange)
                2 -> EditorReliability(profile, onChange)
                3 -> EditorPayloadChaos(profile, onChange)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save") }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditorGeneral(profile: Profile, onChange: (Profile) -> Unit) {
    var tags by remember(profile.tags) { mutableStateOf(profile.tags.joinToString(",")) }

    OutlinedTextField(
        value = profile.name,
        onValueChange = { onChange(profile.copy(name = it)) },
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = profile.description,
        onValueChange = { onChange(profile.copy(description = it)) },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = tags,
        onValueChange = {
            tags = it
            val list = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() }
            onChange(profile.copy(tags = list))
        },
        label = { Text("Tags (comma separated)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorProtocol(
    profile: Profile,
    supported: List<ProtocolType>,
    availability: Map<ProtocolType, Boolean>,
    onChange: (Profile) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = profile.protocolType

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = current.displayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Protocol") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            supported
                .sortedBy { it.orderIndex() }
                .forEach { pt ->
                    val enabled = availability[pt] == true
                    DropdownMenuItem(
                        enabled = enabled,
                        text = {
                            Column {
                                Text(pt.displayName())
                                Text(pt.description(), style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        onClick = {
                            expanded = false
                            onChange(profile.copy(protocolType = pt, transportConfig = defaultTransport(pt)))
                        }
                    )
                }
        }
    }

    val isReady = availability[current] == true
    if (!isReady) {
        Spacer(Modifier.height(8.dp))
        AssistChip(
            onClick = {},
            label = { Text("Coming soon: This protocol has not been registered in the app yet") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        return
    }

    Spacer(Modifier.height(6.dp))

    when (val cfg = profile.transportConfig) {
        is WsOkHttpConfig -> WsOkHttpEditorModern(cfg) { onChange(profile.copy(transportConfig = it)) }
        is WsKtorConfig -> WsKtorEditorModern(cfg) { onChange(profile.copy(transportConfig = it)) }
        is MqttConfig -> MqttEditorModern(cfg) { onChange(profile.copy(transportConfig = it)) }
        is SocketIoConfig -> SocketIoEditorModern(cfg) { onChange(profile.copy(transportConfig = it)) }
        is SignalRConfig -> SignalREditorModern(cfg) { onChange(profile.copy(transportConfig = it)) }
    }
}

@Composable
private fun EditorReliability(profile: Profile, onChange: (Profile) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Enable retry")
        Switch(checked = profile.retryPolicy.enabled, onCheckedChange = { onChange(profile.copy(retryPolicy = profile.retryPolicy.copy(enabled = it))) })
    }
    OutlinedTextField(
        value = profile.retryPolicy.maxAttempts.toString(),
        onValueChange = { onChange(profile.copy(retryPolicy = profile.retryPolicy.copy(maxAttempts = it.toIntOrNull() ?: profile.retryPolicy.maxAttempts))) },
        label = { Text("Retry max attempts") },
        modifier = Modifier.fillMaxWidth(),
        enabled = profile.retryPolicy.enabled
    )
    OutlinedTextField(
        value = profile.retryPolicy.delayMillis.toString(),
        onValueChange = { onChange(profile.copy(retryPolicy = profile.retryPolicy.copy(delayMillis = it.toLongOrNull() ?: profile.retryPolicy.delayMillis))) },
        label = { Text("Delay (ms)") },
        modifier = Modifier.fillMaxWidth(),
        enabled = profile.retryPolicy.enabled
    )
    OutlinedTextField(
        value = profile.retryPolicy.jitterRatio.toString(),
        onValueChange = { onChange(profile.copy(retryPolicy = profile.retryPolicy.copy(jitterRatio = it.toDoubleOrNull() ?: profile.retryPolicy.jitterRatio))) },
        label = { Text("Jitter ratio") },
        modifier = Modifier.fillMaxWidth(),
        enabled = profile.retryPolicy.enabled
    )
}

@Composable
private fun EditorPayloadChaos(profile: Profile, onChange: (Profile) -> Unit) {
    PayloadChaosEditor(
        payloadPolicy = profile.payloadPolicy,
        chaosPolicy = profile.chaosPolicy,
        onPayloadChanged = { onChange(profile.copy(payloadPolicy = it)) },
        onChaosChanged = { onChange(profile.copy(chaosPolicy = it)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportExportDialog(
    title: String,
    ui: ImportExportUi,
    onJsonChange: (String) -> Unit,
    readOnly: Boolean = false,
    onConfirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(
                    value = ui.json,
                    onValueChange = onJsonChange,
                    readOnly = readOnly,
                    minLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text(onConfirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}


private fun ProtocolType.displayName(): String = when (this) {
    ProtocolType.WS_OKHTTP -> "WebSocket (OkHttp)"
    ProtocolType.WS_KTOR -> "WebSocket (Ktor)"
    ProtocolType.MQTT -> "MQTT"
    ProtocolType.SOCKETIO -> "Socket.IO"
    ProtocolType.SIGNALR -> "SignalR"
}

private fun ProtocolType.description(): String = when (this) {
    ProtocolType.WS_OKHTTP -> "Low-latency WS via OkHttp"
    ProtocolType.WS_KTOR -> "WS via Ktor client (CIO)"
    ProtocolType.MQTT -> "Pub/Sub over broker (QoS 0..2)"
    ProtocolType.SOCKETIO -> "Event-based realtime over Socket.IO"
    ProtocolType.SIGNALR -> "Microsoft SignalR hub transport"
}

private fun ProtocolType.orderIndex(): Int = when (this) {
    ProtocolType.WS_OKHTTP -> 0
    ProtocolType.WS_KTOR -> 1
    ProtocolType.MQTT -> 2
    ProtocolType.SOCKETIO -> 3
    ProtocolType.SIGNALR -> 4
}

private fun defaultTransport(pt: ProtocolType): TransportConfig = when (pt) {
    ProtocolType.WS_OKHTTP -> WsOkHttpConfig(endpoint = "wss://echo.websocket.events")
    ProtocolType.WS_KTOR -> WsKtorConfig(endpoint = "wss://echo.websocket.events")
    ProtocolType.MQTT -> MqttConfig(endpoint = "tcp://broker.hivemq.com:1883", clientId = "chatlab-client", topic = "chatlab/messages")
    ProtocolType.SOCKETIO -> SocketIoConfig(endpoint = "https://socketio-chat-h9jt.herokuapp.com", events = listOf("message"))
    ProtocolType.SIGNALR -> SignalRConfig(endpoint = "https://example.com/chathub", hubMethodName = "Send")
}

private data class HeaderRow(val key: String, val value: String)

private fun List<HeaderRow>.toHeadersMap(): Map<String, String> =
    this.map { it.key.trim() to it.value }
        .filter { it.first.isNotBlank() }
        .toMap()

@Composable
private fun HeadersEditor(
    headers: Map<String, String>,
    onChanged: (Map<String, String>) -> Unit
) {
    var rows by remember(headers) {
        mutableStateOf(
            headers.entries.map { HeaderRow(it.key, it.value) }.ifEmpty { listOf(HeaderRow("", "")) }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Headers", style = MaterialTheme.typography.titleMedium)

        rows.forEachIndexed { idx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = row.key,
                    onValueChange = {
                        val n = rows.toMutableList()
                        n[idx] = n[idx].copy(key = it)
                        rows = n
                        onChanged(n.toHeadersMap())
                    },
                    label = { Text("Key") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = row.value,
                    onValueChange = {
                        val n = rows.toMutableList()
                        n[idx] = n[idx].copy(value = it)
                        rows = n
                        onChanged(n.toHeadersMap())
                    },
                    label = { Text("Value") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    val n = rows.toMutableList()
                    if (n.size > 1) n.removeAt(idx) else n[0] = HeaderRow("", "")
                    rows = n
                    onChanged(n.toHeadersMap())
                }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remove header")
                }
            }
        }

        TextButton(onClick = {
            rows = rows + HeaderRow("", "")
        }) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Add header")
        }
    }
}

@Composable
private fun WsOkHttpEditorModern(cfg: WsOkHttpConfig, onChanged: (WsOkHttpConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.pingIntervalMs.toString(), { onChanged(cfg.copy(pingIntervalMs = it.toLongOrNull() ?: cfg.pingIntervalMs)) }, label = { Text("Ping ms") }, modifier = Modifier.fillMaxWidth())
    HeadersEditor(cfg.headers) { onChanged(cfg.copy(headers = it)) }
}

@Composable
private fun WsKtorEditorModern(cfg: WsKtorConfig, onChanged: (WsKtorConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.pingIntervalMs.toString(), { onChanged(cfg.copy(pingIntervalMs = it.toLongOrNull() ?: cfg.pingIntervalMs)) }, label = { Text("Ping ms") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.connectTimeoutMs.toString(), { onChanged(cfg.copy(connectTimeoutMs = it.toLongOrNull() ?: cfg.connectTimeoutMs)) }, label = { Text("Connect timeout ms") }, modifier = Modifier.fillMaxWidth())
    HeadersEditor(cfg.headers) { onChanged(cfg.copy(headers = it)) }
}

@Composable
private fun MqttEditorModern(cfg: MqttConfig, onChanged: (MqttConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Broker endpoint (tcp://host:1883)") }, modifier = Modifier.fill.fillMaxWidth())
    OutlinedTextField(cfg.clientId, { onChanged(cfg.copy(clientId = it)) }, label = { Text("ClientId") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.topic, { onChanged(cfg.copy(topic = it)) }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(
        value = cfg.qos.toString(),
        onValueChange = { onChanged(cfg.copy(qos = (it.toIntOrNull() ?: cfg.qos).coerceIn(0, 2))) },
        label = { Text("QoS (0..2)") },
        modifier = Modifier.fillMaxWidth()
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Clean session")
        Switch(checked = cfg.cleanSession, onCheckedChange = { onChanged(cfg.copy(cleanSession = it)) })
    }

    OutlinedTextField(
        value = cfg.username.orEmpty(),
        onValueChange = { onChanged(cfg.copy(username = it.ifBlank { null })) },
        label = { Text("Username (optional)") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = cfg.password.orEmpty(),
        onValueChange = { onChanged(cfg.copy(password = it.ifBlank { null })) },
        label = { Text("Password (optional)") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    HeadersEditor(cfg.headers) { onChanged(cfg.copy(headers = it)) }
}

@Composable
private fun SocketIoEditorModern(cfg: SocketIoConfig, onChanged: (SocketIoConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.namespace.orEmpty(), { onChanged(cfg.copy(namespace = it.ifBlank { null })) }, label = { Text("Namespace (optional)") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.connectPath.orEmpty(), { onChanged(cfg.copy(connectPath = it.ifBlank { null })) }, label = { Text("Connect path (optional, e.g. /socket.io)") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.events.joinToString(","), { onChanged(cfg.copy(events = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() })) }, label = { Text("Events (csv)") }, modifier = Modifier.fillMaxWidth())
    HeadersEditor(cfg.headers) { onChanged(cfg.copy(headers = it)) }
}

@Composable
private fun SignalREditorModern(cfg: SignalRConfig, onChanged: (SignalRConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Hub URL") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.hubMethodName, { onChanged(cfg.copy(hubMethodName = it)) }, label = { Text("Hub method") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(
        value = cfg.transportPreference.name,
        onValueChange = { },
        readOnly = true,
        label = { Text("Transport preference") },
        modifier = Modifier.fillMaxWidth()
    )
    HeadersEditor(cfg.headers) { onChanged(cfg.copy(headers = it)) }
}
