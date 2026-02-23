package com.msa.chatlab.protocol.signalr

import com.msa.chatlab.core.common.concurrency.AppScope
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * A stable stub implementation for SignalR.
 * The actual implementation is blocked by dependency conflicts (RxJava2 vs project's setup).
 * This stub ensures the app remains stable and buildable.
 */
class SignalRTransport(
    private val profile: Profile,
    private val appScope: AppScope
) : TransportContract {

    override val capabilities = TransportCapabilities(
        supportsQoS = false,
        supportsAck = false,
        supportsNativeReconnect = false,
        supportsBinary = false
    )

    override val connectionState: Flow<ConnectionState> = MutableStateFlow(ConnectionState.Disconnected("SignalR not implemented"))
    override val events: Flow<TransportEvent> = flowOf()
    override val stats: Flow<TransportStatsEvent> = flowOf()

    override suspend fun connect() {
        // No-op: This protocol is not implemented.
    }

    override suspend fun disconnect() {
        // No-op
    }

    override suspend fun send(payload: OutgoingPayload) {
        // No-op
    }
}
