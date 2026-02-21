package com.msa.chatlab.feature.debug.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionLogStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.observability.log.LogStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DebugUiState(
    val activeProfileName: String = "-",
    val connection: String = "-",
    val simulateOffline: Boolean = false,
    val logLines: List<String> = emptyList(),
    val connectionEvents: Int = 0
)

class DebugViewModel(
    private val active: ActiveProfileStore,
    private val cm: ConnectionManager,
    private val connLog: ConnectionLogStore,
    private val appLogs: LogStore
) : ViewModel() {

    private val _state = MutableStateFlow(DebugUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                active.activeProfile,
                cm.connectionState,
                cm.simulateOffline,
                connLog.logFlow,
                appLogs.entries
            ) { p, st, sim, connEvents, logs ->
                DebugUiState(
                    activeProfileName = p?.name ?: "-",
                    connection = st.toString(),
                    simulateOffline = sim,
                    connectionEvents = connEvents.size,
                    logLines = logs.takeLast(200).map { "${it.level} ${it.tag}: ${it.message}" }
                )
            }.collect { _state.value = it }
        }
    }

    fun setSimOffline(enabled: Boolean) {
        cm.setSimulateOffline(enabled)
    }

    fun clearLogs() {
        appLogs.clear()
        connLog.clear()
    }
}