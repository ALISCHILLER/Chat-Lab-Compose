package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.common.concurrency.AppScope
import com.msa.chatlab.core.common.util.Backoff
import com.msa.chatlab.core.data.active.ActiveProfileStore
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

    private val desiredConnected = MutableStateFlow(false)

    private var bindJob: Job? = null
    private var reconnectJob: Job? = null
    private var disconnectRequested = false
    private var lastDisconnectAt: Long = 0L

    init {
        // وقتی پروفایل عوض می‌شود، transport عوض شود.
        scope.launch {
            activeProfileStore.activeProfile
                .distinctUntilChangedBy { it?.id?.value }
                .collectLatest { p ->
                    prepareTransport()
                    // اگر user قصد اتصال داشته، transport جدید را وصل کن
                    if (desiredConnected.value && !_simulateOffline.value) {
                        connectInternal(reason = "profile_switch_autoconnect")
                    }
                }
        }

        // اگر simulateOffline روشن شد، reconnect را متوقف کن
        scope.launch {
            _simulateOffline.collectLatest { offline ->
                if (offline) {
                    reconnectJob?.cancel()
                    reconnectJob = null
                } else {
                    // اگر offline خاموش شد و اتصال مورد انتظار است، reconnect را دوباره فعال کن
                    if (desiredConnected.value && _connectionState.value is ConnectionState.Disconnected && !disconnectRequested) {
                        scheduleReconnect("offline_disabled")
                    }
                }
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

        // disconnect old
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
        desiredConnected.value = true
        disconnectRequested = false
        if (_simulateOffline.value) return

        if (_transport.value == null) {
            prepareTransport()
        }
        connectInternal(reason = "user_connect")
    }

    suspend fun disconnect() {
        desiredConnected.value = false
        disconnectRequested = true

        reconnectJob?.cancel()
        reconnectJob = null

        connectMutex.withLock {
            logger.i("ConnectionManager", "disconnect()")
            _transport.value?.let { runCatching { it.disconnect() } }
        }
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

                    when (st) {
                        is ConnectionState.Connected -> {
                            reconnectJob?.cancel()
                            reconnectJob = null
                        }
                        is ConnectionState.Disconnected -> {
                            lastDisconnectAt = System.currentTimeMillis()
                            if (desiredConnected.value && !disconnectRequested && !_simulateOffline.value) {
                                scheduleReconnect(st.reason ?: "disconnected")
                            }
                        }
                        else -> Unit
                    }
                }
            }
            launch { t.stats.collect { _stats.value = it } }
            launch { t.events.collect { _events.emit(it) } }
        }
    }

    private suspend fun connectInternal(reason: String) {
        connectMutex.withLock {
            if (_simulateOffline.value) return@withLock
            val t = _transport.value ?: run {
                prepareTransport()
                _transport.value ?: error("Transport not prepared")
            }

            logger.i("ConnectionManager", "connectInternal($reason)")
            runCatching { t.connect() }
                .onFailure { ex ->
                    val err = TransportError("CONNECT_FAIL", ex.message ?: "connect failed", ex)
                    crash.record(ex, mapOf("reason" to "connect failed", "stage" to reason))
                    logger.e("ConnectionManager", "connect failed ($reason)", throwable = ex)
                    _events.tryEmit(TransportEvent.ErrorOccurred(err))

                    // اگر کاربر هنوز اتصال می‌خواهد، reconnect را شروع کن
                    if (desiredConnected.value && !disconnectRequested && !_simulateOffline.value) {
                        scheduleReconnect("connect_failed")
                    }
                }
        }
    }

    private fun scheduleReconnect(reason: String) {
        if (reconnectJob?.isActive == true) return

        val profile = activeProfileStore.getActiveNow() ?: return
        val policy = profile.reconnectPolicy
        if (!policy.enabled) return
        if (disconnectRequested) return

        reconnectJob = scope.launch {
            var attempt = 0
            var lastAttemptAt = 0L

            while (isActive && desiredConnected.value && !disconnectRequested) {
                if (_simulateOffline.value) {
                    delay(250)
                    continue
                }

                if (_connectionState.value is ConnectionState.Connected) return@launch
                if (attempt >= policy.maxAttempts) return@launch

                // reset attempt if long time passed
                val now = System.currentTimeMillis()
                if (lastAttemptAt != 0L && (now - lastAttemptAt) > policy.resetAfterMs) {
                    attempt = 0
                }

                attempt += 1
                lastAttemptAt = now

                val delayMs = when (policy.mode) {
                    ReconnectBackoffMode.Fixed ->
                        Backoff.fixed(policy.backoffMs, policy.jitterRatio)
                    ReconnectBackoffMode.Exponential ->
                        Backoff.exponential(
                            attempt = attempt,
                            initialMs = policy.backoffMs,
                            maxMs = policy.maxBackoffMs,
                            jitterRatio = policy.jitterRatio
                        )
                }

                logger.i("ConnectionManager", "reconnect attempt=$attempt in ${delayMs}ms reason=$reason")
                delay(delayMs)

                connectInternal(reason = "reconnect#$attempt")
            }
        }
    }
}
