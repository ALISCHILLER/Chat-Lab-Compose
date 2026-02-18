package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.observability.AppLogger
import com.msa.chatlab.core.observability.CrashReporter
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ConnectionManager(
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

    private var bindJob: Job? = null

    fun isConnectedNow(): Boolean = _connectionState.value is ConnectionState.Connected

    suspend fun send(payload: OutgoingPayload) {
        if (!isConnectedNow()) throw IllegalStateException("Not connected")
        val t = _transport.value ?: throw IllegalStateException("Transport not available")
        t.send(payload)
    }

    suspend fun prepareTransport() {
        logger.i("ConnectionManager", "prepareTransport()")
        crash.breadcrumb("prepareTransport")

        // جلوگیری از leak: bind قبلی cancel شود
        bindJob?.cancel()
        bindJob = null

        _transport.value?.let { runCatching { it.disconnect() } }
        val newTransport = resolver.resolveCurrentTransport()
        _transport.value = newTransport

        bindJob = bindFlows(newTransport)
    }

    suspend fun connect() {
        val t = _transport.value ?: run {
            prepareTransport()
            _transport.value ?: error("Transport not prepared")
        }

        crash.breadcrumb("connect()")
        logger.i("ConnectionManager", "connect()")

        runCatching { t.connect() }.onFailure { ex ->
            val err = TransportError(
                code = "CONNECT_FAIL",
                message = ex.message ?: "connect failed",
                throwable = ex
            )
            crash.record(ex, mapOf("code" to err.code))
            logger.e("ConnectionManager", "connect failed", mapOf("code" to err.code), ex)
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
        }
    }

    suspend fun disconnect() {
        crash.breadcrumb("disconnect()")
        logger.i("ConnectionManager", "disconnect()")
        _transport.value?.let { runCatching { it.disconnect() } }
    }

    private fun bindFlows(t: TransportContract): Job = scope.launch {
        coroutineScope {
            launch {
                t.connectionState.collect { st ->
                    _connectionState.value = st
                    logger.d("Transport", "state=$st")
                    crash.setKey("connection_state", st::class.java.simpleName)
                }
            }
            launch {
                t.stats.collect { s ->
                    _stats.value = s
                }
            }
            launch {
                t.events.collect { ev ->
                    // observability
                    when (ev) {
                        is TransportEvent.Connected -> crash.breadcrumb("TransportEvent.Connected")
                        is TransportEvent.Disconnected -> crash.breadcrumb("TransportEvent.Disconnected", mapOf("reason" to (ev.reason ?: "")))
                        is TransportEvent.MessageSent -> crash.breadcrumb("TransportEvent.MessageSent", mapOf("id" to ev.messageId))
                        is TransportEvent.MessageReceived -> crash.breadcrumb("TransportEvent.MessageReceived", mapOf("id" to ev.payload.envelope.messageId.value))
                        is TransportEvent.ErrorOccurred -> crash.breadcrumb("TransportEvent.Error", mapOf("code" to ev.error.code))
                    }
                    _events.emit(ev)
                }
            }
        }
    }
}
