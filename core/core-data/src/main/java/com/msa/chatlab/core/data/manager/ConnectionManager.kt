package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectionManager(
    private val resolver: ProtocolResolver
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _transport = MutableStateFlow<TransportContract?>(null)
    val transport: StateFlow<TransportContract?> = _transport.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(
        replay = 0,
        extraBufferCapacity = 128
    )
    val events: SharedFlow<TransportEvent> = _events.asSharedFlow()

    private val _stats = MutableStateFlow(TransportStatsEvent())
    val stats: StateFlow<TransportStatsEvent> = _stats.asStateFlow()

    fun isConnectedNow(): Boolean = _connectionState.value is ConnectionState.Connected

    suspend fun send(payload: OutgoingPayload) {
        if (!isConnectedNow()) throw IllegalStateException("Not connected")
        val t = _transport.value ?: throw IllegalStateException("Transport not available")
        t.send(payload)
    }

    /**
     * Transport را بر اساس Active Profile resolve می‌کند.
     * اگر transport قبلی وجود داشته باشد، disconnect می‌کند.
     */
    suspend fun prepareTransport() {
        val current = _transport.value
        if (current != null) {
            runCatching { current.disconnect() }
        }

        val newTransport = resolver.resolveCurrentTransport()
        _transport.value = newTransport

        bindFlows(newTransport)
    }

    suspend fun connect() {
        val t = _transport.value ?: run {
            prepareTransport()
            _transport.value ?: error("Transport not prepared")
        }
        runCatching { t.connect() }
            .onFailure { ex ->
                // اگر خود transport state درست بدهد، اینجا فقط event می‌فرستیم
                _events.tryEmit(TransportEvent.ErrorOccurred(
                    TransportError(
                        code = "CONNECT_FAIL",
                        message = ex.message ?: "connect failed",
                        throwable = ex
                    )
                ))
            }
    }

    suspend fun disconnect() {
        val t = _transport.value ?: return
        runCatching { t.disconnect() }
    }

    private fun bindFlows(t: TransportContract) {
        // اتصال connectionState
        scope.launch {
            t.connectionState.collect { st ->
                _connectionState.value = st
            }
        }
        // اتصال events
        scope.launch {
            t.events.collect { ev ->
                _events.emit(ev)
            }
        }
        // اتصال stats
        scope.launch {
            t.stats.collect { s ->
                _stats.value = s
            }
        }
    }
}
