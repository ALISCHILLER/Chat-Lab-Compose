package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.common.concurrency.AppScope
import com.msa.chatlab.core.common.util.Backoff
import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.domain.model.ReconnectBackoffMode
import com.msa.chatlab.core.observability.crash.CrashReporter
import com.msa.chatlab.core.observability.log.AppLogger
import com.msa.chatlab.core.observability.log.d
import com.msa.chatlab.core.observability.log.e
import com.msa.chatlab.core.observability.log.i
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.msa.chatlab.core.data.active.ActiveProfileStore

class ConnectionManager(
    private val appScope: AppScope,
    private val activeProfileStore: ActiveProfileStore,
    private val resolver: ProtocolResolver,
    private val logger: AppLogger,
    private val crash: CrashReporter
) {
    private val scope: CoroutineScope get() = appScope.scope
    private val connectMutex = Mutex()

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
    private var eventFanoutJob: Job? = null
    private var stateFanoutJob: Job? = null
    private var statsFanoutJob: Job? = null
    private var reconnectJob: Job? = null

    init {
        scope.launch {
            activeProfileStore.activeProfile.collectLatest { profile ->
                connectMutex.withLock {
                    logger.i(this, "Active profile changed to: ${profile?.name ?: "None"}")
                    // Disconnect and cleanup old transport
                    disconnectInternal("Profile changed")
                    // If there's a new profile, resolve and set up its transport
                    if (profile != null) {
                        try {
                            _transport.value = resolver.resolveCurrentTransport()
                            logger.i(this, "Transport resolved for ${profile.name}")
                        } catch (e: Exception) {
                            logger.e(this, e, "Failed to resolve transport for ${profile.name}")
                            crash.report(e, "Transport resolution failed")
                            _transport.value = null
                        }
                    } else {
                        _transport.value = null
                    }
                }
            }
        }
    }

    fun setSimulateOffline(isOffline: Boolean) {
        _simulateOffline.value = isOffline
        if (isOffline) {
            scope.launch { disconnect() }
        }
    }

    suspend fun connect() {
        connectMutex.withLock {
            if (_simulateOffline.value) {
                logger.i(this, "Connect ignored: Offline simulation is active")
                _events.tryEmit(TransportEvent.Error("Simulating offline"))
                return
            }
            val currentTransport = _transport.value
            if (currentTransport == null) {
                logger.e(this, "Connect failed: No active transport")
                _events.tryEmit(TransportEvent.Error("No active profile/transport"))
                return
            }
            if (_connectionState.value is ConnectionState.Connected || _connectionState.value is ConnectionState.Connecting) {
                logger.d(this, "Connect ignored: Already connected or connecting")
                return
            }

            _connectionState.value = ConnectionState.Connecting
            reconnectJob?.cancel()
            eventFanoutJob?.cancel()
            stateFanoutJob?.cancel()
            statsFanoutJob?.cancel()

            stateFanoutJob = scope.launch {
                currentTransport.connectionState.collect { _connectionState.value = it }
            }
            eventFanoutJob = scope.launch {
                currentTransport.events.collect { _events.tryEmit(it) }
            }
            statsFanoutJob = scope.launch {
                currentTransport.stats.collect { _stats.tryEmit(it) }
            }

            scope.launch {
                try {
                    currentTransport.connect()
                    logger.i(this, "Connection successful")
                } catch (e: Exception) {
                    logger.e(this, e, "Connection failed")
                    _events.tryEmit(TransportEvent.Error(e.message ?: "Connection error"))
                    _connectionState.value = ConnectionState.Disconnected("Connection failed: ${e.message}")
                    scheduleReconnect()
                }
            }
        }
    }

    suspend fun disconnect() {
        connectMutex.withLock {
            disconnectInternal("User action")
        }
    }

    suspend fun send(payload: OutgoingPayload) {
        val currentTransport = _transport.value ?: throw TransportError.NotConnected("No transport available")
        if (_connectionState.value !is ConnectionState.Connected) throw TransportError.NotConnected("Transport not connected")

        currentTransport.send(payload)
    }

    private suspend fun disconnectInternal(reason: String) {
        reconnectJob?.cancel()
        val currentTransport = _transport.value
        if (currentTransport != null && _connectionState.value !is ConnectionState.Idle) {
            try {
                currentTransport.disconnect()
            } catch (e: Exception) {
                logger.e(this, e, "Error during disconnect")
            }
        }
        _connectionState.value = ConnectionState.Idle
        eventFanoutJob?.cancel()
        stateFanoutJob?.cancel()
        statsFanoutJob?.cancel()
        logger.d(this, "Disconnected internally. Reason: $reason")
    }

    private fun scheduleReconnect() {
        scope.launch {
            val profile = activeProfileStore.getActiveNow() ?: return@launch
            if (!profile.reconnectPolicy.enabled) return@launch

            reconnectJob = scope.launch {
                var attempt = 1
                while (isActive && _connectionState.value !is ConnectionState.Connected) {
                    val policy = profile.reconnectPolicy
                    val delayMs = when (policy.backoffMode) {
                        ReconnectBackoffMode.Exponential -> Backoff.exponential(
                            attempt = attempt,
                            initial = policy.initialBackoffMs,
                            max = policy.maxBackoffMs,
                            jitter = 0.25
                        )
                        ReconnectBackoffMode.Fixed -> policy.initialBackoffMs
                    }

                    logger.i(this, "Scheduling reconnect attempt #$attempt in ${delayMs}ms")
                    delay(delayMs)

                    if (isActive && _connectionState.value !is ConnectionState.Connected) {
                        logger.i(this, "Executing reconnect attempt #$attempt")
                        connect()
                        attempt++
                    }
                }
            }
        }
    }
}
