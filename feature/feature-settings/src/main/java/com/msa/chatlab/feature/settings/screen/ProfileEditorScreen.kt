package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.feature.settings.component.editor.*
import com.msa.chatlab.feature.settings.state.ProfileEditorState
import com.msa.chatlab.feature.settings.state.SettingsUiEvent
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    editorState: ProfileEditorState,
    validationErrors: List<String>,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val scrollState = rememberScrollState()

    var profile by remember(editorState) {
        mutableStateOf(
            when (editorState) {
                is ProfileEditorState.Creating -> Profile(
                    id = ProfileId(UUID.randomUUID().toString()),
                    name = "New Profile",
                    protocolType = editorState.protocolType,
                    transportConfig = when (editorState.protocolType) {
                        ProtocolType.WS_OKHTTP -> WsOkHttpConfig(endpoint = "wss://echo.websocket.events")
                        ProtocolType.WS_KTOR -> WsKtorConfig(endpoint = "wss://echo.websocket.events")
                        ProtocolType.MQTT -> MqttConfig(
                            endpoint = "tcp://broker.hivemq.com:1883",
                            clientId = "chatlab-${System.currentTimeMillis()}",
                            topic = "chatlab/test"
                        )
                        ProtocolType.SOCKETIO -> SocketIoConfig(endpoint = "https://socketio-chat-h9jt.herokuapp.com")
                        ProtocolType.SIGNALR -> SignalRConfig(endpoint = "https://signalr-demo.azurewebsites.net/chatHub")
                    }
                )
                is ProfileEditorState.Editing -> editorState.profile
                is ProfileEditorState.Closed -> return@mutableStateOf null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (editorState) {
                            is ProfileEditorState.Creating -> "New Profile (${editorState.protocolType.name})"
                            is ProfileEditorState.Editing -> "Edit: ${editorState.profile.name}"
                            is ProfileEditorState.Closed -> "Settings"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.CancelEditor) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (validationErrors.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Validation errors:", style = MaterialTheme.typography.titleSmall, color = Color.Red)
                    validationErrors.forEach { error ->
                        Text("• $error", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onEvent(SettingsUiEvent.DismissValidationErrors) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f))
                    ) {
                        Text("Dismiss")
                    }
                }
            }

            if (editorState is ProfileEditorState.Creating) {
                ProtocolSelector(
                    selectedType = editorState.protocolType,
                    onProtocolSelected = { onEvent(SettingsUiEvent.SelectProtocolType(it)) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            profile?.let {
                ProfileEditorForm(
                    profile = it,
                    onProfileChanged = { updatedProfile ->
                        profile = updatedProfile
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(SettingsUiEvent.CancelEditor) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        profile?.let { onEvent(SettingsUiEvent.SaveProfileClicked(it)) }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Profile")
                }
            }
        }
    }
}

@Composable
private fun ProfileEditorForm(
    profile: Profile,
    onProfileChanged: (Profile) -> Unit
) {
    var name by remember(profile.id) { mutableStateOf(profile.name) }
    var description by remember(profile.id) { mutableStateOf(profile.description) }
    var tags by remember(profile.id) { mutableStateOf(profile.tags.joinToString(",")) }

    LaunchedEffect(name, description, tags) {
        onProfileChanged(
            profile.copy(
                name = name,
                description = description,
                tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            )
        )
    }

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Profile name *") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = tags,
        onValueChange = { tags = it },
        label = { Text("Tags (comma separated)") },
        placeholder = { Text("e.g., production, chat, realtime") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    when (val config = profile.transportConfig) {
        is WsOkHttpConfig -> {
            WsOkHttpEditor(
                config = config,
                onConfigChanged = { newConfig ->
                    onProfileChanged(profile.copy(transportConfig = newConfig))
                }
            )
        }
        is MqttConfig -> {
            MqttEditor(
                config = config,
                onConfigChanged = { newConfig ->
                    onProfileChanged(profile.copy(transportConfig = newConfig))
                }
            )
        }
        else -> {
            Text("Editor for ${profile.protocolType.name} coming soon", color = Color.Gray)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    PayloadChaosEditor(
        payloadProfile = profile.payloadProfile,
        chaosProfile = profile.chaosProfile,
        onPayloadChanged = { newPayload ->
            onProfileChanged(profile.copy(payloadProfile = newPayload))
        },
        onChaosChanged = { newChaos ->
            onProfileChanged(profile.copy(chaosProfile = newChaos))
        }
    )
}
