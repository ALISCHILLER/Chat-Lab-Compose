package com.msa.chatlab.feature.settings.state

data class SettingsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val cards: List<UiProfileCard> = emptyList(),

    val editor: EditorDraft? = null,   // اگر null → در حالت list هستیم

    val showImportDialog: Boolean = false,
    val importText: String = "",

    val showExportDialog: Boolean = false,
    val exportText: String = "",

    val validationErrors: List<String> = emptyList(),
    val lastError: String? = null
)
