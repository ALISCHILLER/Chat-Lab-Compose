package com.msa.chatlab.feature.settings.state

import com.msa.chatlab.core.domain.model.ProtocolType

data class SettingsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val cards: List<UiProfileCard> = emptyList(),

    val editor: ProfileEditorUi? = null,

    val supportedProtocols: List<ProtocolType> = emptyList(),

    val showImportDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val importExport: ImportExportUi = ImportExportUi(),

    val validationErrors: List<String> = emptyList(),
    val lastError: String? = null
)
