package com.msa.chatlab.featuresettings.state

sealed interface SettingsUiEvent {

    // List
    data class SearchChanged(val value: String) : SettingsUiEvent
    data object CreateNew : SettingsUiEvent
    data class Edit(val id: String) : SettingsUiEvent
    data class Duplicate(val id: String) : SettingsUiEvent
    data class Delete(val id: String) : SettingsUiEvent
    data class Apply(val id: String) : SettingsUiEvent

    // Export/Import
    data class Export(val id: String) : SettingsUiEvent
    data object CloseExport : SettingsUiEvent

    data object OpenImport : SettingsUiEvent
    data object CloseImport : SettingsUiEvent
    data class ImportTextChanged(val value: String) : SettingsUiEvent
    data object ImportCommit : SettingsUiEvent

    // Editor
    data object EditorClose : SettingsUiEvent
    data object EditorSave : SettingsUiEvent

    data class EditorName(val value: String) : SettingsUiEvent
    data class EditorDescription(val value: String) : SettingsUiEvent
    data class EditorTags(val value: String) : SettingsUiEvent
    data class EditorProtocol(val value: String) : SettingsUiEvent // از dropdown string میاد
    data class EditorEndpoint(val value: String) : SettingsUiEvent
    data class EditorHeaders(val value: String) : SettingsUiEvent
    data class EditorWsPing(val value: String) : SettingsUiEvent
}
