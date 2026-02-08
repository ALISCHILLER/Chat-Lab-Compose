package com.msa.chatlab.featuresettings.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.msa.chatlab.featuresettings.state.SettingsUiEvent
import com.msa.chatlab.featuresettings.state.SettingsUiState

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    padding: PaddingValues
) {
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {

        if (state.editor == null) {
            ProfileListSection(state, onEvent)
        } else {
            ProfileEditorSection(state, onEvent)
        }

        if (state.showExportDialog) {
            ExportDialog(
                text = state.exportText,
                onClose = { onEvent(SettingsUiEvent.CloseExport) }
            )
        }

        if (state.showImportDialog) {
            ImportDialog(
                text = state.importText,
                onTextChange = { onEvent(SettingsUiEvent.ImportTextChanged(it)) },
                onClose = { onEvent(SettingsUiEvent.CloseImport) },
                onImport = { onEvent(SettingsUiEvent.ImportCommit) }
            )
        }
    }
}
