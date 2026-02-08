package com.msa.chatlab.core.protocol.api.contract

import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.flow.Flow

interface TransportContract {

    val capabilities: TransportCapabilities

    val connectionState: Flow<ConnectionState>
    val events: Flow<TransportEvent>
    val stats: Flow<TransportStatsEvent>

    suspend fun connect()
    suspend fun disconnect()
    suspend fun send(payload: OutgoingPayload)
}
