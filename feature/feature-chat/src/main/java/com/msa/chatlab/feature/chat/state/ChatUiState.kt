package com.msa.chatlab.feature.chat.state

import com.msa.chatlab.feature.chat.model.ChatMessageUi

data class ChatUiState(
    val profileName: String = "",
    val messages: List<ChatMessageUi> = emptyList(),
    val outboxCount: Int = 0,
    val error: String? = null
)
