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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.feature.settings.state.EditorDraft
import com.msa.chatlab.feature.settings.state.SettingsUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    draft: EditorDraft,
    validationErrors: List<String>,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (draft.name.isNotBlank()) "Edit: ${draft.name}" else "New Profile") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.EditorClose) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            ProfileEditorForm(draft = draft, onEvent = onEvent)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(SettingsUiEvent.EditorClose) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onEvent(SettingsUiEvent.EditorSave) },
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
    draft: EditorDraft,
    onEvent: (SettingsUiEvent) -> Unit
) {
    OutlinedTextField(
        value = draft.name,
        onValueChange = { onEvent(SettingsUiEvent.EditorName(it)) },
        label = { Text("Profile name *") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = draft.description,
        onValueChange = { onEvent(SettingsUiEvent.EditorDescription(it)) },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = draft.tagsCsv,
        onValueChange = { onEvent(SettingsUiEvent.EditorTags(it)) },
        label = { Text("Tags (comma separated)") },
        placeholder = { Text("e.g., production, chat, realtime") },
        modifier = Modifier.fillMaxWidth()
    )

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
