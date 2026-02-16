package com.msa.chatlab.feature.chat.model

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
