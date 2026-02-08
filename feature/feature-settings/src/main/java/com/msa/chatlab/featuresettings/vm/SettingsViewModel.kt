package com.msa.chatlab.featuresettings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.featuresettings.state.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(
    private val profileManager: ProfileManager,
    private val activeStore: ActiveProfileStore,
    private val codec: ProfileJsonCodec // همون codec که تو core-data ساختیم
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsUiEffect>(extraBufferCapacity = 16)
    val effects = _effects.asSharedFlow()

    private val profilesFlow = profileManager.observeProfiles()
    private val activeFlow = activeStore.activeProfile

    init {
        viewModelScope.launch {
            combine(
                profilesFlow,
                activeFlow,
                state.map { it.searchQuery }.distinctUntilChanged()
            ) { profiles, active, q ->
                val filtered = if (q.isBlank()) profiles else profiles.filter {
                    it.name.contains(q, ignoreCase = true) ||
                        it.description.contains(q, ignoreCase = true) ||
                        it.tags.any { t -> t.contains(q, ignoreCase = true) }
                }

                val cards = filtered.map {
                    UiProfileCard(
                        id = it.id.value,
                        title = it.name.ifBlank { "(No name)" },
                        subtitle = "${it.protocolType} • ${it.transportConfig.endpoint}",
                        isActive = (active?.id == it.id)
                    )
                }
                _state.value.copy(
                    isLoading = false,
                    cards = cards
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onEvent(ev: SettingsUiEvent) {
        when (ev) {

            is SettingsUiEvent.SearchChanged -> {
                _state.value = _state.value.copy(searchQuery = ev.value)
            }

            SettingsUiEvent.CreateNew -> createNew()
            is SettingsUiEvent.Edit -> openEditor(ev.id)
            is SettingsUiEvent.Duplicate -> duplicate(ev.id)
            is SettingsUiEvent.Delete -> delete(ev.id)
            is SettingsUiEvent.Apply -> apply(ev.id)

            is SettingsUiEvent.Export -> export(ev.id)
            SettingsUiEvent.CloseExport -> _state.value = _state.value.copy(showExportDialog = false, exportText = "")

            SettingsUiEvent.OpenImport -> _state.value = _state.value.copy(showImportDialog = true, importText = "")
            SettingsUiEvent.CloseImport -> _state.value = _state.value.copy(showImportDialog = false, importText = "")
            is SettingsUiEvent.ImportTextChanged -> _state.value = _state.value.copy(importText = ev.value)
            SettingsUiEvent.ImportCommit -> importCommit()

            SettingsUiEvent.EditorClose -> _state.value = _state.value.copy(editor = null, validationErrors = emptyList(), lastError = null)
            SettingsUiEvent.EditorSave -> saveEditor()

            is SettingsUiEvent.EditorName -> updateDraft { it.copy(name = ev.value) }
            is SettingsUiEvent.EditorDescription -> updateDraft { it.copy(description = ev.value) }
            is SettingsUiEvent.EditorTags -> updateDraft { it.copy(tagsCsv = ev.value) }
            is SettingsUiEvent.EditorProtocol -> updateDraft { it.copy(protocolType = ProtocolType.valueOf(ev.value)) }
            is SettingsUiEvent.EditorEndpoint -> updateDraft { it.copy(endpoint = ev.value) }
            is SettingsUiEvent.EditorHeaders -> updateDraft { it.copy(headersText = ev.value) }
            is SettingsUiEvent.EditorWsPing -> updateDraft { it.copy(wsPingIntervalMs = ev.value.toLongOrNull() ?: it.wsPingIntervalMs) }
        }
    }

    // -------------------------
    // Actions
    // -------------------------

    private fun createNew() = viewModelScope.launch {
        // default برای شروع سریع
        val p = profileManager.createDefaultWsOkHttpProfile(
            name = "WS Profile ${UUID.randomUUID().toString().take(4)}",
            endpoint = "wss://echo.websocket.events"
        )
        profileManager.setActive(p)
        _effects.tryEmit(SettingsUiEffect.Toast("Created & applied: ${p.name}"))
    }

    private fun openEditor(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        _state.value = _state.value.copy(
            editor = p.toDraft(),
            validationErrors = emptyList(),
            lastError = null
        )
    }

    private fun duplicate(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        val copy = p.copy(
            id = ProfileId(UUID.randomUUID().toString()),
            name = p.name + " (copy)"
        )
        profileManager.upsert(copy)
        _effects.tryEmit(SettingsUiEffect.Toast("Duplicated"))
    }

    private fun delete(id: String) = viewModelScope.launch {
        runCatching { profileManager.delete(ProfileId(id)) }
            .onSuccess { _effects.tryEmit(SettingsUiEffect.Toast("Deleted")) }
            .onFailure { ex -> _effects.tryEmit(SettingsUiEffect.Toast("Delete failed: ${ex.message}")) }
    }

    private fun apply(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        profileManager.setActive(p)
        _effects.tryEmit(SettingsUiEffect.Toast("Applied: ${p.name}"))
    }

    private fun export(id: String) = viewModelScope.launch {
        val p = profileManager.getProfile(ProfileId(id)) ?: return@launch
        val json = codec.encode(p)
        _state.value = _state.value.copy(showExportDialog = true, exportText = json)
    }

    private fun importCommit() = viewModelScope.launch {
        val text = _state.value.importText.trim()
        if (text.isEmpty()) return@launch

        runCatching {
            val profile = codec.decode(text)
            // اگر id تکراری بود، بهتره id جدید بدیم:
            val safe = profile.copy(id = ProfileId(UUID.randomUUID().toString()))
            profileManager.upsert(safe)
            safe
        }.onSuccess { p ->
            _state.value = _state.value.copy(showImportDialog = false, importText = "")
            _effects.tryEmit(SettingsUiEffect.Toast("Imported: ${p.name}"))
        }.onFailure { ex ->
            _effects.tryEmit(SettingsUiEffect.Toast("Import failed: ${ex.message}"))
        }
    }

    private fun saveEditor() = viewModelScope.launch {
        val draft = _state.value.editor ?: return@launch

        val profile = draft.toProfile()
        val validation = profileManager.validate(profile)

        if (!validation.isValid) {
            _state.value = _state.value.copy(
                validationErrors = validation.errors,
                lastError = "Invalid profile"
            )
            _effects.tryEmit(SettingsUiEffect.Toast("Validation failed"))
            return@launch
        }

        runCatching { profileManager.upsert(profile) }
            .onSuccess {
                _state.value = _state.value.copy(editor = null, validationErrors = emptyList(), lastError = null)
                _effects.tryEmit(SettingsUiEffect.Toast("Saved"))
            }
            .onFailure { ex ->
                _state.value = _state.value.copy(lastError = ex.message ?: "save failed")
                _effects.tryEmit(SettingsUiEffect.Toast("Save failed: ${ex.message}"))
            }
    }

    private fun updateDraft(block: (EditorDraft) -> EditorDraft) {
        val d = _state.value.editor ?: return
        _state.value = _state.value.copy(editor = block(d), validationErrors = emptyList())
    }
}

// -------------------------
// Draft ↔ Domain mapping
// -------------------------

private fun Profile.toDraft(): EditorDraft {
    val headersText = transportConfig.headers.entries.joinToString("\n") { "${it.key}:${it.value}" }

    return EditorDraft(
        id = id.value,
        name = name,
        description = description,
        tagsCsv = tags.joinToString(","),
        protocolType = protocolType,
        endpoint = transportConfig.endpoint,
        headersText = headersText,
        wsPingIntervalMs = (transportConfig as? WsOkHttpConfig)?.pingIntervalMs ?: 15_000
    )
}

private fun EditorDraft.toProfile(): Profile {
    val headers = headersText
        .lines()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) return@mapNotNull null
            val idx = trimmed.indexOf(':')
            if (idx <= 0) return@mapNotNull null
            val k = trimmed.substring(0, idx).trim()
            val v = trimmed.substring(idx + 1).trim()
            if (k.isBlank()) null else k to v
        }.toMap()

    val tags = tagsCsv
        .split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val transport: TransportConfig = when (protocolType) {
        ProtocolType.WS_OKHTTP -> WsOkHttpConfig(
            endpoint = endpoint,
            pingIntervalMs = wsPingIntervalMs,
            headers = headers
        )
        // فعلاً editor فقط ws-okhttp رو کامل پوشش می‌ده؛
        // بقیه رو با endpoint/headers minimal می‌سازیم تا بعداً editor اختصاصی اضافه کنیم.
        ProtocolType.WS_KTOR -> WsKtorConfig(endpoint = endpoint, headers = headers)
        ProtocolType.MQTT -> MqttConfig(endpoint = endpoint, clientId = "client", topic = "topic", headers = headers)
        ProtocolType.SOCKETIO -> SocketIoConfig(endpoint = endpoint, headers = headers)
        ProtocolType.SIGNALR -> SignalRConfig(endpoint = endpoint, headers = headers)
    }

    return Profile(
        id = profileId(),
        name = name,
        description = description,
        tags = tags,
        protocolType = protocolType,
        transportConfig = transport,
        // مقادیر پیش‌فرض
        deliverySemantics = DeliverySemantics.AtLeastOnce,
        ackStrategy = AckStrategy.TransportLevel,
        outboxPolicy = OutboxPolicy(),
        retryPolicy = RetryPolicy(),
        reconnectPolicy = ReconnectPolicy(),
        payloadProfile = PayloadProfile(),
        chaosProfile = ChaosProfile()
    )
}
