package com.msa.chatlab.featureconnect.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.featureconnect.state.ActiveProfileUi
import com.msa.chatlab.featureconnect.state.ConnectUiEffect
import com.msa.chatlab.featureconnect.state.ConnectUiEvent
import com.msa.chatlab.featureconnect.state.ConnectUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager
) : ViewModel() {

    private val _local = MutableStateFlow(ConnectUiState())
    val state = _local.asStateFlow()

    private val _effects = MutableSharedFlow<ConnectUiEffect>(extraBufferCapacity = 16)
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                connectionManager.connectionState,
                activeProfileStore.activeProfile
            ) { cs, ap ->
                val ui = ap?.let {
                    ActiveProfileUi(
                        id = it.id.value,
                        name = it.name,
                        protocol = it.protocolType.name,
                        endpoint = it.transportConfig.endpoint
                    )
                }
                ConnectUiState(
                    connectionState = cs,
                    activeProfile = ui,
                    lastError = _local.value.lastError
                )
            }.collect { merged ->
                _local.value = merged
            }
        }
    }

    fun onEvent(ev: ConnectUiEvent) {
        when (ev) {
            ConnectUiEvent.Prepare -> prepare()
            ConnectUiEvent.Connect -> connect()
            ConnectUiEvent.Disconnect -> disconnect()
            ConnectUiEvent.ClearError -> _local.value = _local.value.copy(lastError = null)
        }
    }

    private fun prepare() = viewModelScope.launch {
        runCatching { connectionManager.prepareTransport() }
            .onFailure { ex ->
                _local.value = _local.value.copy(lastError = ex.message ?: "prepare failed")
                _effects.tryEmit(ConnectUiEffect.Toast("Prepare failed: ${ex.message}"))
            }
    }

    private fun connect() = viewModelScope.launch {
        runCatching { connectionManager.connect() }
            .onFailure { ex ->
                _local.value = _local.value.copy(lastError = ex.message ?: "connect failed")
                _effects.tryEmit(ConnectUiEffect.Toast("Connect failed: ${ex.message}"))
            }
    }

    private fun disconnect() = viewModelScope.launch {
        runCatching { connectionManager.disconnect() }
            .onFailure { ex ->
                _effects.tryEmit(ConnectUiEffect.Toast("Disconnect failed: ${ex.message}"))
            }
    }
}
