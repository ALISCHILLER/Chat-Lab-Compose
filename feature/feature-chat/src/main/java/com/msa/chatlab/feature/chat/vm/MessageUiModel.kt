package com.msa.chatlab.feature.chat.vm

import com.msa.chatlab.core.domain.model.ChatMessage
import com.msa.chatlab.core.domain.model.MessageDirection
import com.msa.chatlab.core.domain.model.MessageStatus

data class MessageUiModel(
    val id: String,
    val text: String,
    val direction: MessageDirection,
    val status: MessageStatus,
    val errorMessage: String?
)

fun ChatMessage.toUiModel(): MessageUiModel {
    return MessageUiModel(
        id = id.value,
        text = text,
        direction = direction,
        status = status,
        errorMessage = errorMessage
    )
}
