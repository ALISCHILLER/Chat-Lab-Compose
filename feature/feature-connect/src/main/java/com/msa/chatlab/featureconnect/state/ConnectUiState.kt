package com.msa.chatlab.featureconnect.state

import com.msa.chatlab.core.protocol.api.contract.ConnectionState

data class ActiveProfileUi(
    val id: String,
    val name: String,
    val protocol: String,
    val endpoint: String
)

data class ConnectUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val activeProfile: ActiveProfileUi? = null,
    val lastError: String? = null
)
