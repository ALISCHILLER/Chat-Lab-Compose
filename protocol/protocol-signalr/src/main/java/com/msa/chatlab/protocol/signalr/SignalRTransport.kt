package com.msa.chatlab.protocol.signalr

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.msa.chatlab.core.data.mapper.decodeToTransportEvent
import com.msa.chatlab.core.domain.model.TransportConfig
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SignalRTransport(
    private val config: TransportConfig
) : TransportContract {

    private var hubConnection: HubConnection? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connectionMutex = Mutex()

    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = false,
        supportsNativeReconnect = false,
        supportsBinary = true
    )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 64)
    override val events: Flow<TransportEvent> = _events.asSharedFlow()

    private val _stats = MutableSharedFlow<TransportStatsEvent>(extraBufferCapacity = 64)
    override val stats: Flow<TransportStatsEvent> = _stats.asSharedFlow()

    override suspend fun connect() {
        connectionMutex.withLock {
            if (_connectionState.value is ConnectionState.Connected || _connectionState.value is ConnectionState.Connecting) {
                return
            }
            _connectionState.value = ConnectionState.Connecting
            Timber.d("SignalR: Connecting to ${config.endpoint}...")

            val connection = HubConnectionBuilder.create(config.endpoint)
                .withHeaders(config.headers)
                .build()
            hubConnection = connection

            registerEventHandlers(connection)

            try {
                connection.start().awaitRx3() // Use Rx3 bridge
                if (connection.connectionState == HubConnectionState.CONNECTED) {
                    _connectionState.value = ConnectionState.Connected()
                    _events.tryEmit(TransportEvent.Connected)
                    Timber.i("SignalR: Connection successful.")
                } else {
                    throw TransportError.ConnectionFailed("Connection failed with state: ${connection.connectionState}")
                }
            } catch (e: Exception) {
                Timber.e(e, "SignalR: Connection attempt failed.")
                val error = TransportError.ConnectionFailed(e.message, e)
                _events.tryEmit(TransportEvent.ErrorOccurred(error))
                _connectionState.value = ConnectionState.Disconnected("Connection failed")
                hubConnection?.stop()?.let { it.awaitRx3() } // Use Rx3 bridge
                hubConnection = null
            }
        }
    }

    override suspend fun disconnect() {
        connectionMutex.withLock {
            if (_connectionState.value is ConnectionState.Disconnected || _connectionState.value is ConnectionState.Idle) {
                return
            }
            Timber.d("SignalR: Disconnecting...")
            val reason = "User initiated disconnect"
            _connectionState.value = ConnectionState.Disconnected(reason)
            _events.tryEmit(TransportEvent.Disconnected(reason))
            hubConnection?.stop()?.let { it.awaitRx3() } // Use Rx3 bridge
            hubConnection = null
            Timber.i("SignalR: Disconnected successfully.")
        }
    }

    override suspend fun send(payload: OutgoingPayload) {
        val connection = hubConnection
        if (connection == null || connection.connectionState != HubConnectionState.CONNECTED) {
            throw TransportError.NotConnected("Cannot send message. Transport is not connected.")
        }

        try {
            Timber.d("SignalR: Sending message via '$SEND_METHOD'")
            connection.send(SEND_METHOD, payload.envelope.body.toString(Charsets.UTF_8))
            _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } catch (e: Exception) {
            Timber.e(e, "SignalR: Failed to send message.")
            throw TransportError.SendFailed("Failed to send message.", e)
        }
    }

    private fun registerEventHandlers(connection: HubConnection) {
        connection.on(RECEIVE_METHOD, { message: String ->
            scope.launch {
                val transportEvent = message.decodeToTransportEvent()
                _events.tryEmit(transportEvent)
            }
        }, String::class.java)

        connection.onClosed { exception ->
            scope.launch {
                connectionMutex.withLock {
                    if (_connectionState.value !is ConnectionState.Disconnected && _connectionState.value !is ConnectionState.Idle) {
                        val reason = "Connection closed unexpectedly: ${exception?.message}"
                        Timber.w(exception, "SignalR: $reason")
                        _connectionState.value = ConnectionState.Disconnected(reason)
                        _events.tryEmit(TransportEvent.Disconnected(reason))
                        hubConnection = null
                    }
                }
            }
        }
    }

    /**
     * Bridges an RxJava 3 [Completable] to a coroutine's [suspend] function.
     * This is a workaround for the dependency mismatch between SignalR (RxJava3)
     * and the project's coroutine extensions (for RxJava2).
     */
    private suspend fun Completable.awaitRx3() {
        suspendCancellableCoroutine<Unit> { cont ->
            val disposable = subscribe(
                { cont.resume(Unit) },
                { err -> cont.resumeWithException(err) }
            )
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }

    private companion object {
        private const val RECEIVE_METHOD = "ReceiveMessage"
        private const val SEND_METHOD = "SendMessage"
    }
}
