package com.msa.chatlab.feature.chat.domain.usecase

import com.msa.chatlab.core.data.manager.MessageSender

class SendMessageUseCase(
    private val messageSender: MessageSender
) {
    suspend operator fun invoke(text: String, destination: String): Result<Unit> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }
        return runCatching {
            messageSender.sendText(trimmed, destination)
        }
    }
}
