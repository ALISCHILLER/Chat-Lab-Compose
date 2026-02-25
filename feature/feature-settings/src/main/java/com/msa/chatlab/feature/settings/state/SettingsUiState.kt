package com.msa.chatlab.feature.settings.state

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType

data class SettingsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val cards: List<UiProfileCard> = emptyList(),

    val supportedProtocols: List<ProtocolType> = emptyList(),
    val protocolAvailability: Map<ProtocolType, Boolean> = emptyMap(),

    val editorProfile: Profile? = null,
    val editorIsNew: Boolean = false,

    val validationErrors: List<String> = emptyList(),

    val showImportDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val importExport: ImportExportUi = ImportExportUi(),

    val pendingDeleteId: String? = null
)
