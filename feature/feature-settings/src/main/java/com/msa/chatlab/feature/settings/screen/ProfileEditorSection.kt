package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.feature.settings.state.SettingsUiEvent
import com.msa.chatlab.feature.settings.state.SettingsUiState

@Composable
fun ProfileEditorSection(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val draft = state.editor ?: return

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

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

        ProtocolDropdown(
            selected = com.msa.chatlab.core.domain.model.ProtocolType.valueOf(draft.protocol),
            onSelect = { onEvent(SettingsUiEvent.EditorProtocol(it.name)) }
        )

        OutlinedTextField(
            value = draft.endpoint,
            onValueChange = { onEvent(SettingsUiEvent.EditorEndpoint(it)) },
            label = { Text("Endpoint") },
            modifier = Modifier.fillMaxWidth()
        )
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
