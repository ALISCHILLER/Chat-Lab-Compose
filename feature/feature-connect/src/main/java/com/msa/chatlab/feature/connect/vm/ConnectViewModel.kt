package com.msa.chatlab.feature.connect.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionLogStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.domain.error.AppError
import com.msa.chatlab.feature.connect.state.ActiveProfileUi
import com.msa.chatlab.feature.connect.state.ConnectUiEffect
import com.msa.chatlab.feature.connect.state.ConnectUiEvent
import com.msa.chatlab.feature.connect.state.ConnectUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager,
    private val logStore: ConnectionLogStore
) : ViewModel() {

    private val _effects = MutableSharedFlow<ConnectUiEffect>(extraBufferCapacity = 16)
    val effects = _effects.asSharedFlow()

    val uiState: StateFlow<ConnectUiState> = combine(
        connectionManager.connectionState,
        activeProfileStore.activeProfile,
        connectionManager.stats,
        logStore.logFlow
    ) { connectionState, activeProfile, stats, logs ->
        val activeProfileUi = activeProfile?.let {
            ActiveProfileUi(
                id = it.id.value,
                name = it.name,
                protocol = it.protocolType.name,
                endpoint = it.transportConfig.endpoint
            )
        }
        ConnectUiState(
            connectionState = connectionState,
            activeProfile = activeProfileUi,
            stats = stats,
            logs = logs
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectUiState())

    fun onEvent(ev: ConnectUiEvent) {
        when (ev) {
            ConnectUiEvent.Prepare -> prepare()
            ConnectUiEvent.Connect -> connect()
            ConnectUiEvent.Disconnect -> disconnect()
            ConnectUiEvent.ClearError -> { /* No-op for now */ }
        }
    }

    private fun prepare() = viewModelScope.launch {
        runCatching { connectionManager.prepareTransport() }
            .onFailure { ex -> _effects.tryEmit(ConnectUiEffect.ShowError(ex.toAppError())) }
    }

    private fun connect() = viewModelScope.launch {
        runCatching { connectionManager.connect() }
            .onFailure { ex -> _effects.tryEmit(ConnectUiEffect.ShowError(ex.toAppError())) }
    }

    private fun disconnect() = viewModelScope.launch {
        runCatching { connectionManager.disconnect() }
            .onFailure { ex -> _effects.tryEmit(ConnectUiEffect.ShowError(ex.toAppError())) }
    }
}

private fun Throwable.toAppError(): AppError {
    // A simple mapper for now
    return AppError.Unknown(this.message ?: "An unknown error occurred")
}
