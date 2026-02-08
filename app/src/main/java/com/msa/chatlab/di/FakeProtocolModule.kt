package com.msa.chatlab.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

private class FakeTransport : TransportContract {
    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = false,
        supportsNativeReconnect = false,
        supportsBinary = false
    )

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 64)
    override val events: Flow<TransportEvent> = _events.asSharedFlow()

    override val stats: Flow<TransportStatsEvent> = flowOf(TransportStatsEvent())

    override suspend fun connect() {
        _state.value = ConnectionState.Connecting
        _state.value = ConnectionState.Connected
        _events.tryEmit(TransportEvent.Connected)
    }

    override suspend fun disconnect() {
        _state.value = ConnectionState.Disconnected("manual")
        _events.tryEmit(TransportEvent.Disconnected("manual", willRetry = false))
    }

    override suspend fun send(payload: OutgoingPayload) {
        _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId))
    }
}

val FakeProtocolModule = module {
    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.WS_OKHTTP
            override fun create(profile: Profile): TransportContract = FakeTransport()
        }
    }
}
