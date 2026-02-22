package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.common.ui.UiMessenger
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val profileName: String? = null,
    val messages: List<MessageUiModel> = emptyList(),
    val outboxCount: Int = 0,
    val error: String? = null
)

class ChatViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val messageRepository: MessageRepository,
    private val outboxQueue: OutboxQueue,
    private val sender: MessageSender,
    private val uiMessenger: UiMessenger,
) : ViewModel() {

    private val errorFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUiState> = activeProfileStore.activeProfile
        .filterNotNull()
        .flatMapLatest { activeProfile ->
            combine(
                messageRepository.observeMessages(activeProfile.id),
                outboxQueue.observeCount(activeProfile.id.value, com.msa.chatlab.core.storage.entity.OutboxStatus.PENDING),
                outboxQueue.observeCount(activeProfile.id.value, com.msa.chatlab.core.storage.entity.OutboxStatus.IN_FLIGHT),
                outboxQueue.observeCount(activeProfile.id.value, com.msa.chatlab.core.storage.entity.OutboxStatus.FAILED),
                errorFlow
            ) { messages, pendingCount, inflightCount, failedCount, error ->
                ChatUiState(
                    profileName = activeProfile.name,
                    messages = messages.map { it.toUiModel() },
                    outboxCount = pendingCount + inflightCount + failedCount,
                    error = error
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun send(text: String, destination: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            runCatching { sender.sendText(trimmed, destination) }
                .onFailure { errorFlow.value = it.message ?: "Send failed" }
        }
    }
}
