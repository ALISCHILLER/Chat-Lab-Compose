package com.msa.chatlab.feature.connect.state

import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent

data class ActiveProfileUi(
    val id: String,
    val name: String,
    val protocol: String,
    val endpoint: String
)

data class ConnectUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val activeProfile: ActiveProfileUi? = null,
    val stats: TransportStatsEvent = TransportStatsEvent(),
    val logs: List<TransportEvent> = emptyList(),
    val lastError: String? = null
)
