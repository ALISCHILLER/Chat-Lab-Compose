package com.msa.chatlab.protocol.ws.ktor

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.WsKtorConfig
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.mapper.decodeToTransportEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.headers
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

class KtorTransport(
    private val profile: Profile,
    private val now: () -> Long = { System.currentTimeMillis() }
) : TransportContract {

    override val capabilities: TransportCapabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = false,
        supportsNativeReconnect = false,
        supportsBinary = true
    )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 256)
    override val events: Flow<TransportEvent> = _events.asSharedFlow()

    private val _stats = MutableStateFlow(TransportStatsEvent())
    override val stats: Flow<TransportStatsEvent> = _stats.asStateFlow()

    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var client: HttpClient? = null
    private var session: WebSocketSession? = null
    private var readerJob: Job? = null

    private fun requireConfig(): WsKtorConfig {
        val cfg = profile.transportConfig
        require(cfg is WsKtorConfig) { "Profile transportConfig is not WsKtorConfig" }
        return cfg
    }

    override suspend fun connect() {
        if (!started.compareAndSet(false, true)) return

        val cfg = requireConfig()
        _connectionState.value = ConnectionState.Connecting

        try {
            val http = HttpClient(CIO) {
                install(WebSockets) { pingInterval = cfg.pingIntervalMs.milliseconds }
                install(HttpTimeout) { connectTimeoutMillis = cfg.connectTimeoutMs }
            }
            client = http

            val wsSession = http.webSocketSession {
                url(cfg.endpoint)
                headers { cfg.headers.forEach { (k, v) -> append(k, v) } }
            }
            session = wsSession

            _connectionState.value = ConnectionState.Connected(at = now())
            _events.tryEmit(TransportEvent.Connected)

            readerJob = scope.launch {
                try {
                    for (frame in wsSession.incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                _events.tryEmit(text.decodeToTransportEvent())
                                _stats.value = _stats.value.copy(
                                    bytesReceived = _stats.value.bytesReceived + text.encodeToByteArray().size
                                )
                            }

                            is Frame.Binary -> {
                                val bytes = frame.readBytes()
                                val text = runCatching { bytes.toString(Charsets.UTF_8) }.getOrNull()
                                if (text != null) {
                                    _events.tryEmit(text.decodeToTransportEvent())
                                }
                                _stats.value = _stats.value.copy(bytesReceived = _stats.value.bytesReceived + bytes.size)
                            }

                            is Frame.Close -> break
                            else -> Unit
                        }
                    }
                } catch (e: Exception) {
                    if (started.get()) {
                        _events.tryEmit(TransportEvent.ErrorOccurred(TransportError.ConnectionFailed(e.message, e)))
                    }
                } finally {
                    if (started.get()) {
                        started.set(false)
                        _connectionState.value = ConnectionState.Disconnected("Ktor WS ended", at = now())
                        _events.tryEmit(TransportEvent.Disconnected("Ktor WS ended"))
                    }
                }
            }
        } catch (e: Exception) {
            started.set(false)
            _connectionState.value = ConnectionState.Disconnected(e.message, at = now())
            _events.tryEmit(TransportEvent.ErrorOccurred(TransportError.ConnectionFailed(e.message, e)))
            throw e
        }
    }

    override suspend fun disconnect() {
        started.set(false)
        readerJob?.cancel()
        readerJob = null

        val wsSession = session
        session = null
        runCatching {
            wsSession?.close(CloseReason(CloseReason.Codes.NORMAL, "client disconnect"))
        }

        client?.close()
        client = null

        _connectionState.value = ConnectionState.Disconnected("client disconnect", at = now())
        _events.tryEmit(TransportEvent.Disconnected("client disconnect"))
    }

    override suspend fun send(payload: OutgoingPayload) {
        val wsSession = session ?: run {
            val err = TransportError.NotConnected("Ktor WS not connected")
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
            throw err
        }

        try {
            val bytes = payload.envelope.body
            val ct = payload.envelope.contentType

            if (ct.startsWith("text/")) {
                wsSession.send(Frame.Text(bytes.decodeToString()))
            } else {
                wsSession.send(Frame.Binary(fin = true, data = bytes))
            }

            _stats.value = _stats.value.copy(bytesSent = _stats.value.bytesSent + bytes.size)
            _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } catch (e: Exception) {
            val err = TransportError.SendFailed(e.message, e)
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
            throw err
        }
    }
}
