package com.msa.chatlab.feature.chat.state

data class ChatMessage(
    val id: String,
    val from: String,
    val text: String,
    val ts: Long
)

data class ChatUiState(
    val profileName: String = "No profile",
    val messages: List<ChatMessage> = emptyList(),
    val outboxCount: Int = 0,
    val error: String? = null
)
