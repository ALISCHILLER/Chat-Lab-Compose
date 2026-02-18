package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.model.MessageDirection
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.feature.chat.model.ChatMessageUi
import com.msa.chatlab.feature.chat.state.ChatUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val messageRepository: MessageRepository,
    private val outboxQueue: OutboxQueue
) : ViewModel() {

    private val errorFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChatUiState> = activeProfileStore.activeProfile
        .filterNotNull()
        .flatMapLatest { activeProfile ->
            combine(
                messageRepository.observeMessages(activeProfile.id),
                outboxQueue.observe(),
                errorFlow
            ) { messages, outbox, error ->
                ChatUiState(
                    profileName = activeProfile.name,
                    messages = messages.map { it.toUiModel() },
                    outboxCount = outbox.size,
                    error = error
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun clearError() {
        errorFlow.value = null
    }

    fun send(text: String, destination: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val activeProfile = activeProfileStore.getActiveNow() ?: run {
                errorFlow.value = "No active profile"
                return@launch
            }

            val messageId = MessageId(UUID.randomUUID().toString())
            messageRepository.insertOutgoing(
                profileId = activeProfile.id,
                messageId = messageId,
                text = trimmed,
                destination = destination
            )

            outboxQueue.enqueue(
                com.msa.chatlab.core.data.outbox.OutboxItem(
                    id = UUID.randomUUID().toString(),
                    messageId = messageId.value,
                    destination = destination,
                    contentType = "text/plain",
                    headersJson = "{}",
                    body = trimmed.toByteArray(),
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}

private fun com.msa.chatlab.core.domain.model.ChatMessage.toUiModel(): ChatMessageUi {
    return ChatMessageUi(
        id = this.id.value,
        direction = if (this.direction == MessageDirection.IN) ChatMessageUi.Direction.IN else ChatMessageUi.Direction.OUT,
        text = this.text,
        timeMs = this.localCreatedAt.value
    )
}
