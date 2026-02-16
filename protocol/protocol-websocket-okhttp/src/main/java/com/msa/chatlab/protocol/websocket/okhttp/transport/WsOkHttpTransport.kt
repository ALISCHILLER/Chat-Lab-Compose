package com.msa.chatlab.protocol.websocket.okhttp.transport

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.protocol.websocket.okhttp.config.WsOkHttpConfigAdapter
import com.msa.chatlab.protocol.websocket.okhttp.config.WsOkHttpConfigAdapter.toOkHttpClient
import com.msa.chatlab.protocol.websocket.okhttp.mapper.OkHttpErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean

class WsOkHttpTransport(
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

    private var ws: WebSocket? = null
    private val listener by lazy {
        WsOkHttpWebSocketListener(
            connectionState = _connectionState,
            events = _events,
            stats = _stats,
            now = now
        )
    }

    override suspend fun connect() {
        if (!started.compareAndSet(false, true)) {
            return
        }

        _connectionState.value = ConnectionState.Connecting

        val request = WsOkHttpConfigAdapter.buildRequest(profile)
        val client = WsOkHttpConfigAdapter.requireConfig(profile).toOkHttpClient()

        ws = client.newWebSocket(request, listener)
    }

    override suspend fun disconnect() {
        started.set(false)
        val socket = ws
        ws = null

        if (socket != null) {
            socket.close(1000, "client disconnect")
        } else {
            _connectionState.value = ConnectionState.Disconnected("no socket")
        }
    }

    override suspend fun send(payload: OutgoingPayload) {
        val socket = ws ?: run {
            val err = OkHttpErrorMapper.map(IllegalStateException("WebSocket not connected"))
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
            throw IllegalStateException("WebSocket not connected")
        }

        val bytes = payload.envelope.body
        val contentType = payload.envelope.contentType

        val ok = if (contentType.startsWith("text/")) {
            socket.send(bytes.decodeToString())
        } else {
            socket.send(ByteString.of(*bytes))
        }

        if (ok) {
            _stats.value = _stats.value.copy(bytesSent = _stats.value.bytesSent + bytes.size)
            _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } else {
            val err = OkHttpErrorMapper.map(RuntimeException("OkHttp send returned false"))
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
        }
    }
}
