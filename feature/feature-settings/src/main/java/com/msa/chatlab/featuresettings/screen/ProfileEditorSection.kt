package com.msa.chatlab.featuresettings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.featuresettings.state.SettingsUiEvent
import com.msa.chatlab.featuresettings.state.SettingsUiState

@Composable
fun ProfileEditorSection(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val draft = state.editor ?: return

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onEvent(SettingsUiEvent.EditorClose) }) { Text("Back") }
            Button(onClick = { onEvent(SettingsUiEvent.EditorSave) }) { Text("Save") }
        }

        Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)

        if (state.validationErrors.isNotEmpty()) {
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Validation Errors", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    state.validationErrors.forEach { Text("• $it", color = MaterialTheme.colorScheme.error) }
                }
            }
        }

        OutlinedTextField(
            value = draft.name,
            onValueChange = { onEvent(SettingsUiEvent.EditorName(it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = draft.description,
            onValueChange = { onEvent(SettingsUiEvent.EditorDescription(it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = draft.tagsCsv,
            onValueChange = { onEvent(SettingsUiEvent.EditorTags(it)) },
            label = { Text("Tags (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        ProtocolDropdown(
            selected = draft.protocolType,
            onSelect = { onEvent(SettingsUiEvent.EditorProtocol(it.name)) }
        )

        OutlinedTextField(
            value = draft.endpoint,
            onValueChange = { onEvent(SettingsUiEvent.EditorEndpoint(it)) },
            label = { Text("Endpoint") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = draft.headersText,
            onValueChange = { onEvent(SettingsUiEvent.EditorHeaders(it)) },
            label = { Text("Headers (Key:Value per line)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        if (draft.protocolType == ProtocolType.WS_OKHTTP) {
            OutlinedTextField(
                value = draft.wsPingIntervalMs.toString(),
                onValueChange = { onEvent(SettingsUiEvent.EditorWsPing(it)) },
                label = { Text("Ping interval ms") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Protocol-specific editor for ${draft.protocolType} will be added next.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProtocolDropdown(
    selected: ProtocolType,
    onSelect: (ProtocolType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val all = remember { ProtocolType.entries }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Protocol") }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            all.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        expanded = false
                        onSelect(type)
                    }
                )
            }
        }
    }
}
