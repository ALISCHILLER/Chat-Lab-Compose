package com.msa.chatlab.core.protocolapi.contract

import com.msa.chatlab.core.protocolapi.event.TransportEvent
import com.msa.chatlab.core.protocolapi.event.TransportStatsEvent
import com.msa.chatlab.core.protocolapi.payload.OutgoingPayload
import kotlinx.coroutines.flow.Flow

interface TransportContract {

    val capabilities: TransportCapabilities

    /** وضعیت اتصال (state machine) */
    val connectionState: Flow<ConnectionState>

    /** رویدادهای لحظه‌ای (برای Debug/Timeline) */
    val events: Flow<TransportEvent>

    /** آمار سبک (برای metrics) */
    val stats: Flow<TransportStatsEvent>

    suspend fun connect()

    suspend fun disconnect()

    suspend fun send(payload: OutgoingPayload)
}
