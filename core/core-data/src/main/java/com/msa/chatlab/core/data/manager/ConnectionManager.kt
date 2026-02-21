package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.observability.crash.CrashReporter
import com.msa.chatlab.core.observability.log.AppLogger
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ConnectionManager(
    private val activeProfileStore: ActiveProfileStore,
    private val resolver: ProtocolResolver,
    private val logger: AppLogger,
    private val crash: CrashReporter
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _transport = MutableStateFlow<TransportContract?>(null)
    val transport: StateFlow<TransportContract?> = _transport.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 256)
    val events: SharedFlow<TransportEvent> = _events.asSharedFlow()

    private val _stats = MutableStateFlow(TransportStatsEvent())
    val stats: StateFlow<TransportStatsEvent> = _stats.asStateFlow()

    private val _simulateOffline = MutableStateFlow(false)
    val simulateOffline: StateFlow<Boolean> = _simulateOffline.asStateFlow()

    private var bindJob: Job? = null
    private var reconnectJob: Job? = null
    private var disconnectRequested = false

    init {
        // ✅ auto switch transport when active profile changes
        scope.launch {
            activeProfileStore.activeProfile
                .distinctUntilChangedBy { it?.id?.value }
                .collectLatest {
                    prepareTransport()
                }
        }
    }

    fun setSimulateOffline(enabled: Boolean) {
        _simulateOffline.value = enabled
    }

    fun isConnectedNow(): Boolean = _connectionState.value is ConnectionState.Connected

    suspend fun prepareTransport() {
        bindJob?.cancel()
        bindJob = null

        reconnectJob?.cancel()
        reconnectJob = null

        _transport.value?.let { runCatching { it.disconnect() } }

        val profile = activeProfileStore.getActiveNow()
        if (profile == null) {
            _transport.value = null
            _connectionState.value = ConnectionState.Idle
            return
        }

        val newTransport = resolver.resolveCurrentTransport()
        _transport.value = newTransport
        bindJob = bindFlows(newTransport)
    }

    suspend fun connect() {
        disconnectRequested = false

        val t = _transport.value ?: run {
            prepareTransport()
            _transport.value ?: error("Transport not prepared")
        }

        logger.i("ConnectionManager", "connect()")

        runCatching { t.connect() }.onFailure { ex ->
            val err = TransportError("CONNECT_FAIL", ex.message ?: "connect failed", ex)
            crash.record(ex, mapOf("reason" to "connect failed"))
            logger.e("ConnectionManager", "connect failed", tr = ex)
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
        }
    }

    suspend fun disconnect() {
        disconnectRequested = true
        reconnectJob?.cancel()
        reconnectJob = null

        logger.i("ConnectionManager", "disconnect()")
        _transport.value?.let { runCatching { it.disconnect() } }
    }

    suspend fun send(payload: OutgoingPayload) {
        if (_simulateOffline.value) throw IllegalStateException("Simulated offline")
        if (!isConnectedNow()) throw IllegalStateException("Not connected")
        val t = _transport.value ?: throw IllegalStateException("Transport not available")
        t.send(payload)
    }

    private fun bindFlows(t: TransportContract): Job = scope.launch {
        coroutineScope {
            launch {
                t.connectionState.collect { st ->
                    _connectionState.value = st
                    logger.d("Transport", "state=$st")

                    if (st is ConnectionState.Disconnected) maybeReconnect()
                }
            }
            launch { t.stats.collect { _stats.value = it } }
            launch { t.events.collect { _events.emit(it) } }
        }
    }

    private fun maybeReconnect() {
        if (disconnectRequested) return
        if (reconnectJob?.isActive == true) return

        val profile = activeProfileStore.getActiveNow() ?: return
        val policy = profile.reconnectPolicy
        if (!policy.enabled) return

        reconnectJob = scope.launch {
            var attempt = 0
            while (isActive && !disconnectRequested) {
                if (_connectionState.value is ConnectionState.Connected) return@launch
                if (attempt >= policy.maxAttempts) return@launch
                attempt++
                delay(policy.backoffMs)

                runCatching { _transport.value?.connect() }
            }
        }
    }
}