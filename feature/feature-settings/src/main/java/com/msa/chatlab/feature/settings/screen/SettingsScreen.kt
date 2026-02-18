package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.designsystem.component.ProfileCard
import com.msa.chatlab.feature.settings.R
import com.msa.chatlab.feature.settings.state.SettingsUiEvent
import com.msa.chatlab.feature.settings.state.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    if (state.editor != null) {
        ProfileEditorScreen(
            draft = state.editor,
            supportedProtocols = state.supportedProtocols,
            validationErrors = state.validationErrors,
            onEvent = onEvent
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_profiles_title)) },
                actions = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.OpenImport) }) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = stringResource(R.string.settings_import_action))
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = stringResource(R.string.settings_export_all_action))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(SettingsUiEvent.CreateNew) }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.settings_new_profile_action))
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = scaffoldPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.cards) { card ->
                ProfileCard(
                    name = card.title,
                    protocol = card.subtitle.split(" • ").firstOrNull() ?: "",
                    endpoint = card.subtitle.split(" • ").getOrNull(1) ?: "",
                    isActive = card.isActive,
                    onClick = { onEvent(SettingsUiEvent.Edit(card.id)) },
                    onMenuClick = { /* TODO: Show dropdown menu */ }
                )
            }
        }
    }

    if (state.showImportDialog) {
        ImportDialog(
            text = state.importText,
            onTextChange = { onEvent(SettingsUiEvent.ImportTextChanged(it)) },
            onClose = { onEvent(SettingsUiEvent.CloseImport) },
            onImport = { onEvent(SettingsUiEvent.ImportCommit) }
        )
    }

    if (state.showExportDialog) {
        ExportDialog(
            text = state.exportText,
            onClose = { onEvent(SettingsUiEvent.CloseExport) }
        )
    }
}
