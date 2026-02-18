package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.feature.settings.R
import com.msa.chatlab.feature.settings.state.EditorDraft
import com.msa.chatlab.feature.settings.state.SettingsUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    draft: EditorDraft,
    supportedProtocols: List<ProtocolType>,
    validationErrors: List<String>,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (draft.name.isNotBlank()) stringResource(R.string.editor_edit_profile_title, draft.name) else stringResource(R.string.editor_new_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.EditorClose) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.editor_back_action))
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
                        onClick = { onEvent(SettingsUiEvent.ImportCommit) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f))
                    ) {
                        Text("Dismiss")
                    }
                }
            }

            ProfileEditorForm(
                draft = draft,
                supportedProtocols = supportedProtocols,
                onEvent = onEvent
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(SettingsUiEvent.EditorClose) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.editor_cancel_action))
                }
                Button(
                    onClick = { onEvent(SettingsUiEvent.EditorSave) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.editor_save_action))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditorForm(
    draft: EditorDraft,
    supportedProtocols: List<ProtocolType>,
    onEvent: (SettingsUiEvent) -> Unit
) {
    var isProtocolDropdownExpanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = draft.name,
        onValueChange = { onEvent(SettingsUiEvent.EditorName(it)) },
        label = { Text(stringResource(R.string.editor_name_label)) },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    ExposedDropdownMenuBox(
        expanded = isProtocolDropdownExpanded,
        onExpandedChange = { isProtocolDropdownExpanded = it }
    ) {
        OutlinedTextField(
            value = draft.protocolType.name,
            onValueChange = {},
            label = { Text(stringResource(R.string.editor_protocol_label)) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProtocolDropdownExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = isProtocolDropdownExpanded,
            onDismissRequest = { isProtocolDropdownExpanded = false }
        ) {
            supportedProtocols.forEach { protocol ->
                DropdownMenuItem(
                    text = { Text(protocol.name) },
                    onClick = {
                        onEvent(SettingsUiEvent.EditorProtocol(protocol.name))
                        isProtocolDropdownExpanded = false
                    }
                )
            }
            (ProtocolType.values().toList() - supportedProtocols.toSet()).forEach { protocol ->
                DropdownMenuItem(
                    text = { Text("${protocol.name} (Coming soon)") },
                    onClick = {},
                    enabled = false
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = draft.endpoint,
        onValueChange = { onEvent(SettingsUiEvent.EditorEndpoint(it)) },
        label = { Text("Endpoint *") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = draft.headersText,
        onValueChange = { onEvent(SettingsUiEvent.EditorHeaders(it)) },
        label = { Text("Headers (Key:Value format)") },
        modifier = Modifier.fillMaxWidth().height(120.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (draft.protocolType == ProtocolType.WS_OKHTTP) {
        OutlinedTextField(
            value = draft.wsPingIntervalMs.toString(),
            onValueChange = { onEvent(SettingsUiEvent.EditorWsPing(it)) },
            label = { Text("Ping Interval (ms)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
