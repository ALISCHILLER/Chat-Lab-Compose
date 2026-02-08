package com.msa.chatlab.featurechat.state

import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.featurechat.model.ChatMessageUi

data class ChatUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val activeProfileName: String = "No active profile",
    val input: String = "",
    val messages: List<ChatMessageUi> = emptyList(),
    val lastEvent: String? = null,
    val error: String? = null,
    val outboxCount: Int = 0,
    val simulateOffline: Boolean = false
)
