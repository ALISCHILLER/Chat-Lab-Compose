package com.msa.chatlab.feature.chat.state

data class ChatItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long
)
