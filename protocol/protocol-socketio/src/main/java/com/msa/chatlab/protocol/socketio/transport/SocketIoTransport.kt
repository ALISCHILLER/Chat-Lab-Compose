package com.msa.chatlab.protocol.socketio.transport

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.SocketIoConfig
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.protocol.socketio.config.toSocketOptions
import com.msa.chatlab.protocol.socketio.mapper.toIncomingPayload
import com.msa.chatlab.protocol.socketio.mapper.toTransportError
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SocketIoTransport(
    private val profile: Profile
) : TransportContract {

    private val config = profile.transportConfig as SocketIoConfig
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 64)
    override val events: Flow<TransportEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState

    private var socket: Socket? = null

    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = true, // Socket.IO has built-in ack mechanism
        supportsNativeReconnect = false,
        supportsBinary = true
    )

    override suspend fun connect() {
        if (socket?.connected() == true) return
        _connectionState.value = ConnectionState.Connecting
        try {
            val options = config.toSocketOptions()
            socket = IO.socket(config.endpoint, options)
            socket?.on(Socket.EVENT_CONNECT) { 
                _connectionState.value = ConnectionState.Connected(System.currentTimeMillis())
                scope.launch { _events.emit(TransportEvent.Connected) }
            }?.on(Socket.EVENT_DISCONNECT) { args ->
                val reason = args.firstOrNull()?.toString() ?: "Unknown reason"
                _connectionState.value = ConnectionState.Disconnected(reason)
                scope.launch { _events.emit(TransportEvent.Disconnected(reason)) }
            }?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = (args.firstOrNull() as? Throwable) ?: IllegalStateException("Connection error")
                _connectionState.value = ConnectionState.Disconnected(error.message)
                scope.launch { _events.emit(TransportEvent.ErrorOccurred(error.toTransportError())) }
            }
            config.events.forEach { eventName ->
                socket?.on(eventName) { args ->
                    args.firstOrNull()?.let {
                        scope.launch { _events.emit(TransportEvent.MessageReceived(it.toIncomingPayload(eventName))) }
                    }
                }
            }
            socket?.connect()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Disconnected(e.message)
            throw e.toTransportError()
        }
    }

    override suspend fun disconnect() {
        socket?.disconnect()
    }

    override suspend fun send(payload: OutgoingPayload) {
        val currentSocket = socket ?: throw IllegalStateException("Not connected")
        val eventName = payload.destination ?: config.events.firstOrNull() ?: "message"
        currentSocket.emit(eventName, payload.envelope.body)
        _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
    }
}
