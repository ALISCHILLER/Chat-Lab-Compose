package com.msa.chatlab.feature.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.feature.settings.state.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
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
        _state.update { it.copy(supportedProtocols = ProtocolType.values().filter { t -> registry.has(t) }) }

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
                        id = it.id.value,
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

    fun onEvent(ev: SettingsUiEvent) {
        when (ev) {
            is SettingsUiEvent.SearchChanged -> _state.update { it.copy(searchQuery = ev.value) }

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
        }
    }

    private fun openNew() = viewModelScope.launch {
        val p = profileManager.createDefaultWsOkHttpProfile(
            name = "Profile ${UUID.randomUUID().toString().take(4)}",
            endpoint = "wss://echo.websocket.events"
        )
        // فقط برای draft در UI، ذخیره واقعی با Save انجام می‌شود:
        _state.update { it.copy(editorProfile = p.copy(id = ProfileId(UUID.randomUUID().toString())), editorIsNew = true) }
    }

    private fun openEditor(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
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

    private fun apply(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        activeStore.setActive(p)
        _effects.tryEmit(SettingsUiEffect.Toast("Applied: ${p.name}"))
    }

    private fun duplicate(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        val copy = p.copy(id = ProfileId(UUID.randomUUID().toString()), name = p.name + " (copy)")
        profileManager.upsert(copy)
        _effects.tryEmit(SettingsUiEffect.Toast("Duplicated"))
    }

    private fun confirmDelete() = viewModelScope.launch {
        val id = _state.value.pendingDeleteId ?: return@launch
        runCatching { profileManager.delete(ProfileId(id)) }
            .onSuccess {
                _state.update { it.copy(pendingDeleteId = null) }
                _effects.tryEmit(SettingsUiEffect.Toast("Deleted"))
            }
            .onFailure { ex ->
                _effects.tryEmit(SettingsUiEffect.Toast("Delete failed: ${ex.message}"))
            }
    }

    private fun exportProfile(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        val json = codec.encode(p)
        _state.update { it.copy(showExportDialog = true, importExport = ImportExportUi(json = json)) }
    }

    private fun exportAll() = viewModelScope.launch {
        val profiles = profileManager.observeProfiles().first()
        val arr = JSONArray()
        profiles.forEach { arr.put(codec.encode(it)) }
        _state.update { it.copy(showExportDialog = true, importExport = ImportExportUi(json = arr.toString())) }
    }

    private fun importCommit() = viewModelScope.launch {
        val raw = _state.value.importExport.json.trim()
        if (raw.isBlank()) return@launch

        runCatching {
            val imported = mutableListOf<Profile>()
            if (raw.startsWith("[")) {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val p = codec.decode(arr.getString(i)).copy(id = ProfileId(UUID.randomUUID().toString()))
                    profileManager.upsert(p)
                    imported += p
                }
            } else {
                val p = codec.decode(raw).copy(id = ProfileId(UUID.randomUUID().toString()))
                profileManager.upsert(p)
                imported += p
            }
            imported
        }.onSuccess { list ->
            _state.update { it.copy(showImportDialog = false, importExport = ImportExportUi()) }
            _effects.tryEmit(SettingsUiEffect.Toast("Imported: ${list.size} profile(s)"))
        }.onFailure { ex ->
            _state.update { it.copy(importExport = it.importExport.copy(error = ex.message ?: "Import failed")) }
        }
    }
}