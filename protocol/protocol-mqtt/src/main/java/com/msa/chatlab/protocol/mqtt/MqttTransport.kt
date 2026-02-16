package com.msa.chatlab.protocol.mqtt

import android.content.Context
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.TransportCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class MqttTransport(private val context: Context) : TransportContract {
    override val capabilities = TransportCapabilities(isImplemented = false)
    override val connectionState: Flow<ConnectionState> = MutableStateFlow(ConnectionState.Disconnected("MQTT not implemented"))
    override val events: Flow<TransportEvent> = flowOf()
    override val stats: Flow<TransportStatsEvent> = flowOf()

    override suspend fun connect() {}
    override suspend fun disconnect() {}
    override suspend fun send(payload: OutgoingPayload) {}
}