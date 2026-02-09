package com.msa.chatlab.protocol.signalr.transport

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.SignalRConfig
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.protocol.signalr.config.toHubConnectionBuilder
import com.msa.chatlab.protocol.signalr.mapper.toIncomingPayload
import com.msa.chatlab.protocol.signalr.mapper.toTransportError
import com.microsoft.signalr.HubConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SignalRTransport(
    private val profile: Profile
) : TransportContract {

    private val config = profile.transportConfig as SignalRConfig
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 64)
    override val events: Flow<TransportEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState

    private var hubConnection: HubConnection? = null

    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = false,
        supportsNativeReconnect = true,
        supportsBinary = true
    )

    override suspend fun connect() {
        if (hubConnection?.connectionState == com.microsoft.signalr.HubConnectionState.CONNECTED) return
        _connectionState.value = ConnectionState.Connecting
        try {
            hubConnection = config.toHubConnectionBuilder().build()
            hubConnection?.on(config.hubMethodName, { message ->
                scope.launch { _events.emit(TransportEvent.MessageReceived(message.toIncomingPayload(config.hubMethodName))) }
            }, String::class.java)
            hubConnection?.onConnected { 
                _connectionState.value = ConnectionState.Connected(System.currentTimeMillis())
                scope.launch { _events.emit(TransportEvent.Connected) }
            }
            hubConnection?.onClosed { 
                _connectionState.value = ConnectionState.Disconnected("Closed")
                scope.launch { _events.emit(TransportEvent.Disconnected("Closed")) }
            }
            hubConnection?.start()?.blockingGet()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Disconnected(e.message)
            throw e.toTransportError()
        }
    }

    override suspend fun disconnect() {
        hubConnection?.stop()
    }

    override suspend fun send(payload: OutgoingPayload) {
        val currentHub = hubConnection ?: throw IllegalStateException("Not connected")
        try {
            currentHub.send(config.hubMethodName, String(payload.envelope.body))
            _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } catch (e: Exception) {
            throw e.toTransportError()
        }
    }
}
