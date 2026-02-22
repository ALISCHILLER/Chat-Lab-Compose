package com.msa.chatlab.feature.chat.model

import com.msa.chatlab.core.domain.model.ChatMessage
import com.msa.chatlab.core.domain.model.MessageDirection

data class ChatMessageUi(
    val id: String,
    val direction: Direction,
    val text: String,
    val timeMs: Long,

    // ✅ 3.2
    val queued: Boolean = false,
    val attempt: Int = 0
) {
    enum class Direction { OUT, IN }
}

fun ChatMessage.toChatMessageUi(): ChatMessageUi = ChatMessageUi(
    id = this.id.value,
    direction = if (this.direction == MessageDirection.OUT) ChatMessageUi.Direction.OUT else ChatMessageUi.Direction.IN,
    text = this.text,
    timeMs = this.localCreatedAt.value
)
