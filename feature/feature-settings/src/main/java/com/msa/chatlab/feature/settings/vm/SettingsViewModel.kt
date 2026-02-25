package com.msa.chatlab.feature.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.model.WsOkHttpConfig
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.feature.settings.state.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.util.UUID

class SettingsViewModel(
    private val profileManager: ProfileManager,
    private val activeStore: ActiveProfileStore,
    private val codec: ProfileJsonCodec,
    private val registry: ProtocolRegistry
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsUiEffect>(extraBufferCapacity = 16)
    val effects = _effects.asSharedFlow()

    init {
        val all = ProtocolType.values().toList()
        _state.update {
            it.copy(
                supportedProtocols = all,
                protocolAvailability = all.associateWith { t -> registry.has(t) }
            )
        }

        viewModelScope.launch {
            combine(
                profileManager.observeProfiles(),
                activeStore.activeProfile,
                state.map { it.searchQuery }.distinctUntilChanged()
            ) { profiles, active, q ->
                val filtered = if (q.isBlank()) profiles else profiles.filter {
                    it.name.contains(q, true) ||
                            it.description.contains(q, true) ||
                            it.tags.any { t -> t.contains(q, true) }
                }

                val cards = filtered.map {
                    UiProfileCard(
                        id = it.id,
                        title = it.name.ifBlank { "(No name)" },
                        subtitle = "${it.protocolType.name} • ${it.transportConfig.endpoint}",
                        isActive = (active?.id == it.id)
                    )
                }

                _state.value.copy(
                    isLoading = false,
                    cards = cards
                )
            }.collect { _state.value = it }
        }
    }

    fun onEvent(ev: SettingsUiEvent) = viewModelScope.launch {
        when (ev) {
            is SettingsUiEvent.SearchQuery -> _state.update { it.copy(searchQuery = ev.query) }
            SettingsUiEvent.NewProfile -> openNew()
            is SettingsUiEvent.Edit -> openEditor(ev.id)
            is SettingsUiEvent.Apply -> apply(ev.id)
            is SettingsUiEvent.Duplicate -> duplicate(ev.id)

            is SettingsUiEvent.RequestDelete -> _state.update { it.copy(pendingDeleteId = ev.id) }
            SettingsUiEvent.DismissDelete -> _state.update { it.copy(pendingDeleteId = null) }
            SettingsUiEvent.ConfirmDelete -> confirmDelete()

            SettingsUiEvent.OpenImport -> _state.update { it.copy(showImportDialog = true, importExport = ImportExportUi()) }
            SettingsUiEvent.CloseImport -> _state.update { it.copy(showImportDialog = false, importExport = ImportExportUi()) }
            is SettingsUiEvent.ImportTextChanged -> _state.update { it.copy(importExport = it.importExport.copy(json = ev.value, error = null)) }
            SettingsUiEvent.ImportCommit -> importCommit()

            SettingsUiEvent.ExportAll -> exportAll()
            is SettingsUiEvent.ExportProfile -> exportProfile(ev.id)
            SettingsUiEvent.CloseExport -> _state.update { it.copy(showExportDialog = false, importExport = ImportExportUi()) }

            is SettingsUiEvent.EditorChanged -> _state.update { it.copy(editorProfile = ev.profile, validationErrors = emptyList()) }
            SettingsUiEvent.EditorSave -> saveEditor()
            SettingsUiEvent.EditorClose -> _state.update { it.copy(editorProfile = null, editorIsNew = false, validationErrors = emptyList()) }
            is SettingsUiEvent.SearchQuery -> TODO()
            SettingsUiEvent.NewProfile -> TODO()
            is SettingsUiEvent.Edit -> TODO()
            is SettingsUiEvent.Apply -> TODO()
            is SettingsUiEvent.Duplicate -> TODO()
            is SettingsUiEvent.RequestDelete -> TODO()
            SettingsUiEvent.DismissDelete -> TODO()
            SettingsUiEvent.ConfirmDelete -> TODO()
            SettingsUiEvent.OpenImport -> TODO()
            SettingsUiEvent.CloseImport -> TODO()
            is SettingsUiEvent.ImportTextChanged -> TODO()
            SettingsUiEvent.ImportCommit -> TODO()
            SettingsUiEvent.ExportAll -> TODO()
            is SettingsUiEvent.ExportProfile -> TODO()
            SettingsUiEvent.CloseExport -> TODO()
            is SettingsUiEvent.EditorChanged -> TODO()
            SettingsUiEvent.EditorSave -> TODO()
            SettingsUiEvent.EditorClose -> TODO()
        }
    }

    private fun openNew() = viewModelScope.launch {
        val p = Profile(
            id = ProfileId(UUID.randomUUID().toString()),
            name = "Profile ${UUID.randomUUID().toString().take(4)}",
            protocolType = ProtocolType.WS_OKHTTP,
            transportConfig = WsOkHttpConfig(endpoint = "wss://echo.websocket.events")
        )
        _state.update {
            it.copy(
                editorProfile = p,
                editorIsNew = true,
                validationErrors = emptyList()
            )
        }
    }

    private fun openEditor(id: ProfileId) = viewModelScope.launch {
        val p = profileManager.getProfile(id) ?: return@launch
        _state.update { it.copy(editorProfile = p, editorIsNew = false, validationErrors = emptyList()) }
    }

    private fun saveEditor() = viewModelScope.launch {
        val p = _state.value.editorProfile ?: return@launch

        val validation = profileManager.validate(p)
        if (!validation.isValid) {
            _state.update { it.copy(validationErrors = validation.errors.map { e -> e.message }) }
            _effects.tryEmit(SettingsUiEffect.Toast("Validation failed"))
            return@launch
        }

        runCatching { profileManager.upsert(p) }
            .onSuccess {
                _state.update { it.copy(editorProfile = null, editorIsNew = false, validationErrors = emptyList()) }
                _effects.tryEmit(SettingsUiEffect.Toast("Saved"))
            }
            .onFailure { ex ->
                _effects.tryEmit(SettingsUiEffect.Toast("Save failed: ${ex.message}"))
            }
    }

    private fun apply(id: ProfileId) = viewModelScope.launch {
        activeStore.setActive(id)
        _effects.tryEmit(SettingsUiEffect.Toast("Profile applied"))
    }

    private fun duplicate(id: ProfileId) = viewModelScope.launch {
        val p = profileManager.getProfile(id)
        if (p != null) {
            val new = p.copy(
                id = ProfileId(UUID.randomUUID().toString()),
                name = p.name + " (copy)"
            )
            profileManager.upsert(new)
            _effects.tryEmit(SettingsUiEffect.Toast("Duplicated"))
        }
    }

    private fun confirmDelete() = viewModelScope.launch {
        val id = _state.value.pendingDeleteId ?: return@launch
        profileManager.delete(id)
        _state.update { it.copy(pendingDeleteId = null) }
        _effects.tryEmit(SettingsUiEffect.Toast("Deleted"))
    }

    private fun exportAll() = viewModelScope.launch {
        val profiles = profileManager.getProfiles()
        val jsonElements = profiles.map { codec.toJsonElement(it) }
        val jsonArray = JsonArray(jsonElements)
        _state.update { it.copy(showExportDialog = true, importExport = ImportExportUi(json = Json.encodeToString(JsonArray.serializer(), jsonArray))) }
    }

    private fun exportProfile(id: ProfileId) = viewModelScope.launch {
        val p = profileManager.getProfile(id)
        if (p != null) {
            val json = codec.toPrettyJson(p)
            _state.update { it.copy(showExportDialog = true, importExport = ImportExportUi(json = json)) }
        }
    }

    private fun importCommit() = viewModelScope.launch {
        val json = _state.value.importExport.json.trim()
        if (json.isBlank()) return@launch

        runCatching {
            val profiles = if (json.startsWith("[")) {
                val jsonArray = Json.decodeFromString<JsonArray>(json)
                jsonArray.map { codec.fromJsonElement(it.jsonObject) }
            } else {
                listOf(codec.fromJson(json))
            }

            profiles.forEach { profileManager.upsert(it) }
            _state.update { it.copy(showImportDialog = false) }
            _effects.tryEmit(SettingsUiEffect.Toast("Imported ${profiles.size} profiles"))
        }.onFailure { ex ->
            _state.update { it.copy(importExport = it.importExport.copy(error = ex.message)) }
        }
    }
}
