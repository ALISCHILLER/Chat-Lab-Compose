package com.msa.chatlab.protocol.websocket.ktor.transport

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.WsKtorConfig
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.protocol.websocket.ktor.config.toHttpClient
import com.msa.chatlab.protocol.websocket.ktor.mapper.toIncomingPayload
import com.msa.chatlab.protocol.websocket.ktor.mapper.toTransportError
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KtorTransport(
    private val profile: Profile
) : TransportContract {

    private val config = profile.transportConfig as WsKtorConfig
    private val client = config.toHttpClient()
    private var session: DefaultClientWebSocketSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 64)
    override val events: Flow<TransportEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState

    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = false,
        supportsNativeReconnect = false,
        supportsBinary = true
    )

    override suspend fun connect() {
        if (session?.isActive == true) return
        _connectionState.value = ConnectionState.Connecting
        try {
            client.webSocket(config.endpoint) {
                session = this
                _connectionState.value = ConnectionState.Connected(System.currentTimeMillis())
                _events.emit(TransportEvent.Connected)

                incoming.consumeAsFlow().collect { frame ->
                    frame.toIncomingPayload()?.let { payload ->
                        _events.emit(TransportEvent.MessageReceived(payload))
                    }
                }
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Disconnected(e.message)
            throw e.toTransportError()
        }
    }

    override suspend fun disconnect() {
        session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnect"))
        session = null
        _connectionState.value = ConnectionState.Idle
    }

    override suspend fun send(payload: OutgoingPayload) {
        val currentSession = session ?: throw IllegalStateException("Not connected")
        try {
            currentSession.send(payload.envelope.body)
            _events.emit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } catch (e: Exception) {
            throw e.toTransportError()
        }
    }

    fun cleanup() {
        scope.cancel()
        client.close()
    }
}
