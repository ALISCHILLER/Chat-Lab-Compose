package com.msa.chatlab.feature.settings.state

import com.msa.chatlab.core.domain.model.Profile

sealed interface SettingsUiEvent {

    data class SearchChanged(val value: String) : SettingsUiEvent

    data object NewProfile : SettingsUiEvent
    data class Edit(val id: String) : SettingsUiEvent
    data class Apply(val id: String) : SettingsUiEvent
    data class Duplicate(val id: String) : SettingsUiEvent

    data class RequestDelete(val id: String) : SettingsUiEvent
    data object ConfirmDelete : SettingsUiEvent
    data object DismissDelete : SettingsUiEvent

    data object OpenImport : SettingsUiEvent
    data object CloseImport : SettingsUiEvent
    data class ImportTextChanged(val value: String) : SettingsUiEvent
    data object ImportCommit : SettingsUiEvent

    data object ExportAll : SettingsUiEvent
    data class ExportProfile(val id: String) : SettingsUiEvent
    data object CloseExport : SettingsUiEvent

    data class EditorChanged(val profile: Profile) : SettingsUiEvent
    data object EditorSave : SettingsUiEvent
    data object EditorClose : SettingsUiEvent
}