package com.msa.chatlab.featurechat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.data.settings.ActiveProfileStore
import com.msa.chatlab.featurechat.state.ChatMessage
import com.msa.chatlab.featurechat.state.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val activeProfileStore: ActiveProfileStore, // فعلاً استفاده نمی‌کنیم تا observeActiveProfile خطا نده
    private val outboxQueue: OutboxQueue,
    private val messageSender: MessageSender
) : ViewModel() {

    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val errorFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUiState> =
        combine(
            messagesFlow,
            errorFlow,
            outboxQueue.observe()
        ) { msgs, err, outbox ->
            ChatUiState(
                profileName = "Active",   // بعداً می‌تونی به activeProfileStore وصلش کنی
                messages = msgs,
                outboxCount = outbox.size,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun onInputChange(newValue: String) {
        // قبلاً تو Route صدا زده می‌شده؛ الان ورودی رو تو UI نگه می‌داریم → no-op
    }

    fun clearError() {
        errorFlow.value = null
    }

    fun toggleSimulateOffline() {
        // قابلیت قدیمی بوده → فعلاً no-op تا کامپایل بشه
    }

    fun send(text: String, destination: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val localMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            from = "me",
            text = trimmed,
            ts = System.currentTimeMillis()
        )

        viewModelScope.launch {
            messagesFlow.value = messagesFlow.value + localMsg
            errorFlow.value = null

            runCatching {
                messageSender.sendText(trimmed, destination)
            }.onFailure {
                errorFlow.value = it.message ?: "Send failed"
            }
        }
    }
}
