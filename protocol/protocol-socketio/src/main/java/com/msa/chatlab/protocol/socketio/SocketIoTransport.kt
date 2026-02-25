package com.msa.chatlab.protocol.socketio

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.SocketIoConfig
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.UUID

class SocketIoTransport(
    private val profile: Profile,
    private val okHttpClient: OkHttpClient
) : TransportContract {

    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = true, // Socket.IO supports acks
        supportsNativeReconnect = true, // Socket.IO has built-in reconnect
        supportsBinary = true
    )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 256)
    override val events: Flow<TransportEvent> = _events.asSharedFlow()

    private val _stats = MutableStateFlow(TransportStatsEvent())
    override val stats: Flow<TransportStatsEvent> = _stats.asStateFlow()

    private var socket: Socket? = null

    private fun requireConfig(): SocketIoConfig {
        val cfg = profile.transportConfig
        require(cfg is SocketIoConfig) { "Profile transportConfig is not SocketIoConfig" }
        return cfg
    }

    override suspend fun connect() {
        if (socket?.connected() == true) return

        _connectionState.value = ConnectionState.Connecting

        val cfg = requireConfig()
        val opts = IO.Options()
        opts.callFactory = okHttpClient
        opts.webSocketFactory = okHttpClient

        val newSocket = IO.socket(cfg.endpoint, opts)
        socket = newSocket

        newSocket.on(Socket.EVENT_CONNECT) { _connectionState.value = ConnectionState.Connected() }
        newSocket.on(Socket.EVENT_DISCONNECT) { _connectionState.value = ConnectionState.Disconnected("Socket.IO disconnected") }
        newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val reason = (args.firstOrNull() as? Exception)?.message ?: "Unknown connect error"
            _connectionState.value = ConnectionState.Disconnected(reason)
            _events.tryEmit(TransportEvent.ErrorOccurred(TransportError.ConnectionFailed(reason)))
        }

        cfg.events.forEach { eventName ->
            newSocket.on(eventName) { args ->
                val data = args.firstOrNull()?.toString() ?: ""
                val envelope = Envelope(
                    messageId = MessageId(UUID.randomUUID().toString()),
                    createdAt = TimestampMillis(System.currentTimeMillis()),
                    contentType = "text/plain",
                    headers = emptyMap(),
                    body = data.toByteArray()
                )
                val payload = IncomingPayload(envelope = envelope, source = eventName)
                _events.tryEmit(TransportEvent.MessageReceived(payload))
                _stats.value = _stats.value.copy(bytesReceived = _stats.value.bytesReceived + payload.envelope.body.size)
            }
        }

        newSocket.connect()
    }

    override suspend fun disconnect() {
        socket?.disconnect()
    }

    override suspend fun send(payload: OutgoingPayload) {
        val s = socket ?: throw IllegalStateException("Socket.IO not connected")

        val data = if (payload.envelope.contentType.startsWith("application/json")) {
            JSONObject(payload.envelope.body.decodeToString())
        } else {
            payload.envelope.body
        }

        val cfg = requireConfig()
        val eventName = payload.destination?.takeIf { it.isNotBlank() } ?: cfg.events.firstOrNull() ?: "message"
        s.emit(eventName, data)
        _stats.value = _stats.value.copy(bytesSent = _stats.value.bytesSent + payload.envelope.body.size)
        _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
    }
}
