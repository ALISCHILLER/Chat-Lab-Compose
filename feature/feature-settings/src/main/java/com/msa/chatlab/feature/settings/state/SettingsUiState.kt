package com.msa.chatlab.feature.settings.state

import com.msa.chatlab.core.domain.model.ProtocolType

data class SettingsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val cards: List<UiProfileCard> = emptyList(),

    val editor: EditorDraft? = null,   // اگر null → در حالت list هستیم

    val supportedProtocols: List<ProtocolType> = emptyList(),

    val showImportDialog: Boolean = false,
    val importText: String = "",

    val showExportDialog: Boolean = false,
    val exportText: String = "",

    val validationErrors: List<String> = emptyList(),
    val lastError: String? = null
)
