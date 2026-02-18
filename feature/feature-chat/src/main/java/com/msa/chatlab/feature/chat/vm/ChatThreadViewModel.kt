package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatThreadUiState(
    val profileName: String = "No profile",
    val messages: List<MessageEntity> = emptyList(),
    val error: String? = null
)

class ChatThreadViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val repo: MessageRepository,
    private val sender: MessageSender
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    fun state(destination: String): StateFlow<ChatThreadUiState> =
        activeProfileStore.activeProfile.flatMapLatest { profile ->
            if (profile == null) flowOf(ChatThreadUiState(profileName = "No profile"))
            else combine(
                repo.observeConversation(profile.id, destination),
                error
            ) { msgs, err ->
                ChatThreadUiState(profileName = profile.name, messages = msgs, error = err)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatThreadUiState())

    fun send(destination: String, text: String) {
        viewModelScope.launch {
            runCatching { sender.sendText(text, destination) }
                .onFailure { error.value = it.message ?: "Send failed" }
        }
    }
}
