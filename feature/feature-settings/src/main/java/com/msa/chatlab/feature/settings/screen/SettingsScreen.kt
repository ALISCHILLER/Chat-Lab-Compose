package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.feature.settings.component.common.SearchBar
import com.msa.chatlab.feature.settings.state.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var menuForId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Profiles") },
                actions = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.OpenImport) }) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = "Import")
                    }
                    IconButton(onClick = { onEvent(SettingsUiEvent.ExportAll) }) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = "Export all")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(SettingsUiEvent.NewProfile) }) {
                Icon(Icons.Outlined.Add, contentDescription = "New")
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(SettingsUiEvent.SearchChanged(it)) }
            )

            if (state.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.cards, key = { it.id }) { card ->
                        ProfileCardModern(
                            card = card,
                            onClick = { onEvent(SettingsUiEvent.Edit(card.id)) },
                            onOverflow = { menuForId = card.id }
                        )

                        DropdownMenu(
                            expanded = menuForId == card.id,
                            onDismissRequest = { menuForId = null }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Activate") },
                                enabled = !card.isActive,
                                onClick = { menuForId = null; onEvent(SettingsUiEvent.Apply(card.id)) },
                                leadingIcon = { Icon(Icons.Outlined.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { menuForId = null; onEvent(SettingsUiEvent.Edit(card.id)) },
                                leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = { menuForId = null; onEvent(SettingsUiEvent.Duplicate(card.id)) },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export") },
                                onClick = { menuForId = null; onEvent(SettingsUiEvent.ExportProfile(card.id)) },
                                leadingIcon = { Icon(Icons.Outlined.FileDownload, null) }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { menuForId = null; onEvent(SettingsUiEvent.RequestDelete(card.id)) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { onEvent(SettingsUiEvent.DismissDelete) },
            confirmButton = {
                Button(onClick = { onEvent(SettingsUiEvent.ConfirmDelete) }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(SettingsUiEvent.DismissDelete) }) { Text("Cancel") }
            },
            title = { Text("Delete profile?") },
            text = { Text("This action cannot be undone.") }
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

    // ✅ Editor sheet (product-style)
    state.editorProfile?.let { profile ->
        ProfileEditorBottomSheet(
            profile = profile,
            supported = state.supportedProtocols,
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
    onOverflow: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
    validationErrors: List<String>,
    onChange: (Profile) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("General", "Protocol", "Reliability", "Payload & Chaos")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                1 -> EditorProtocol(profile, supported, onChange)
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
private fun EditorProtocol(profile: Profile, supported: List<ProtocolType>, onChange: (Profile) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = profile.protocolType.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Protocol") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            supported.forEach { pt ->
                DropdownMenuItem(
                    text = { Text(pt.name) },
                    onClick = {
                        expanded = false
                        onChange(profile.copy(protocolType = pt, transportConfig = defaultTransport(pt)))
                    }
                )
            }
        }
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
    OutlinedTextField(
        value = profile.retryPolicy.maxAttempts.toString(),
        onValueChange = { onChange(profile.copy(retryPolicy = profile.retryPolicy.copy(maxAttempts = it.toIntOrNull() ?: profile.retryPolicy.maxAttempts))) },
        label = { Text("Retry max attempts") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = profile.retryPolicy.initialBackoffMs.toString(),
        onValueChange = { onChange(profile.copy(retryPolicy = profile.retryPolicy.copy(initialBackoffMs = it.toLongOrNull() ?: profile.retryPolicy.initialBackoffMs))) },
        label = { Text("Initial backoff (ms)") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = profile.reconnectPolicy.backoffMs.toString(),
        onValueChange = { onChange(profile.copy(reconnectPolicy = profile.reconnectPolicy.copy(backoffMs = it.toLongOrNull() ?: profile.reconnectPolicy.backoffMs))) },
        label = { Text("Reconnect backoff (ms)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorPayloadChaos(profile: Profile, onChange: (Profile) -> Unit) {
    var codecExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = codecExpanded, onExpandedChange = { codecExpanded = it }) {
        OutlinedTextField(
            value = profile.payloadProfile.codec.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Codec") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codecExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = codecExpanded, onDismissRequest = { codecExpanded = false }) {
            CodecMode.values().forEach { cm ->
                DropdownMenuItem(
                    text = { Text(cm.name) },
                    onClick = {
                        codecExpanded = false
                        onChange(profile.copy(payloadProfile = profile.payloadProfile.copy(codec = cm)))
                    }
                )
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(
            checked = profile.chaosProfile.enabled,
            onCheckedChange = { onChange(profile.copy(chaosProfile = profile.chaosProfile.copy(enabled = it))) }
        )
        Spacer(Modifier.width(10.dp))
        Text("Chaos enabled")
    }

    if (profile.chaosProfile.enabled) {
        OutlinedTextField(
            value = profile.chaosProfile.dropRatePercent.toString(),
            onValueChange = { onChange(profile.copy(chaosProfile = profile.chaosProfile.copy(dropRatePercent = it.toDoubleOrNull() ?: 0.0))) },
            label = { Text("Drop rate %") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = profile.chaosProfile.delayMinMs.toString(),
            onValueChange = { onChange(profile.copy(chaosProfile = profile.chaosProfile.copy(delayMinMs = it.toLongOrNull() ?: 0))) },
            label = { Text("Delay min (ms)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = profile.chaosProfile.delayMaxMs.toString(),
            onValueChange = { onChange(profile.copy(chaosProfile = profile.chaosProfile.copy(delayMaxMs = it.toLongOrNull() ?: 0))) },
            label = { Text("Delay max (ms)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ImportExportDialog(
    title: String,
    ui: ImportExportUi,
    onJsonChange: (String) -> Unit,
    readOnly: Boolean = false,
    onConfirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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

private fun defaultTransport(pt: ProtocolType): TransportConfig = when (pt) {
    ProtocolType.WS_OKHTTP -> WsOkHttpConfig(endpoint = "wss://echo.websocket.events")
    ProtocolType.WS_KTOR -> WsKtorConfig(endpoint = "wss://echo.websocket.events")
    ProtocolType.MQTT -> MqttConfig(endpoint = "tcp://broker.hivemq.com:1883", clientId = "chatlab-client", topic = "chatlab/messages")
    ProtocolType.SOCKETIO -> SocketIoConfig(endpoint = "https://socketio-chat-h9jt.herokuapp.com", events = listOf("message"))
    ProtocolType.SIGNALR -> SignalRConfig(endpoint = "https://example.com/chathub", hubMethodName = "Send")
}

/** ✅ minimal modern editors (to keep this file self-contained) */
@Composable private fun WsOkHttpEditorModern(cfg: WsOkHttpConfig, onChanged: (WsOkHttpConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.pingIntervalMs.toString(), { onChanged(cfg.copy(pingIntervalMs = it.toLongOrNull() ?: cfg.pingIntervalMs)) }, label = { Text("Ping ms") }, modifier = Modifier.fillMaxWidth())
}
@Composable private fun WsKtorEditorModern(cfg: WsKtorConfig, onChanged: (WsKtorConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.connectTimeoutMs.toString(), { onChanged(cfg.copy(connectTimeoutMs = it.toLongOrNull() ?: cfg.connectTimeoutMs)) }, label = { Text("Connect timeout ms") }, modifier = Modifier.fillMaxWidth())
}
@Composable private fun MqttEditorModern(cfg: MqttConfig, onChanged: (MqttConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Broker endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.clientId, { onChanged(cfg.copy(clientId = it)) }, label = { Text("ClientId") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.topic, { onChanged(cfg.copy(topic = it)) }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth())
}
@Composable private fun SocketIoEditorModern(cfg: SocketIoConfig, onChanged: (SocketIoConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Endpoint") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.events.joinToString(","), { onChanged(cfg.copy(events = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() })) }, label = { Text("Events (csv)") }, modifier = Modifier.fillMaxWidth())
}
@Composable private fun SignalREditorModern(cfg: SignalRConfig, onChanged: (SignalRConfig) -> Unit) {
    OutlinedTextField(cfg.endpoint, { onChanged(cfg.copy(endpoint = it)) }, label = { Text("Hub URL") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(cfg.hubMethodName, { onChanged(cfg.copy(hubMethodName = it)) }, label = { Text("Hub method") }, modifier = Modifier.fillMaxWidth())
}
