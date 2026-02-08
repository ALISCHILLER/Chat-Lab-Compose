package com.msa.chatlab.featurechat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.featurechat.model.ChatMessageUi
import com.msa.chatlab.featurechat.state.ChatUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager,
    private val messageSender: MessageSender,
    private val outboxQueue: OutboxQueue
) : ViewModel() {

    private val _input = MutableStateFlow("")
    private val _messages = MutableStateFlow<List<ChatMessageUi>>(emptyList())
    private val _lastEvent = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private val outboxCountFlow = outboxQueue.observe().map { it.size }
    private val simulateOfflineFlow = messageSender.simulateOffline

    val uiState: StateFlow<ChatUiState> = combine(
        connectionManager.connectionState,
        activeProfileStore.activeProfile,
        _input,
        _messages,
        _lastEvent,
        _error,
        outboxCountFlow,
        simulateOfflineFlow
    ) { conn, profile, input, msgs, last, err, outboxCount, simOffline ->
        ChatUiState(
            connectionState = conn,
            activeProfileName = profile?.name ?: "No active profile",
            input = input,
            messages = msgs,
            lastEvent = last,
            error = err,
            outboxCount = outboxCount,
            simulateOffline = simOffline
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    init {
        viewModelScope.launch {
            connectionManager.events.collect { ev ->
                _lastEvent.value = when (ev) {
                    is TransportEvent.Connected -> "Connected"
                    is TransportEvent.Disconnected -> "Disconnected: ${ev.reason}"
                    is TransportEvent.MessageSent -> "Sent: ${ev.messageId}"
                    is TransportEvent.MessageReceived -> "Received"
                    is TransportEvent.ErrorOccurred -> "Error: ${ev.error.code}"
                }

                when (ev) {
                    is TransportEvent.MessageReceived -> {
                        val text = ev.payload.envelope.body.toString(Charsets.UTF_8)
                        appendMessage(ChatMessageUi.Direction.IN, text, queued = false)
                    }
                    is TransportEvent.ErrorOccurred -> _error.value = ev.error.message
                    else -> Unit
                }
            }
        }
    }

    fun onInputChange(v: String) { _input.value = v }
    fun clearError() { _error.value = null }

    fun toggleSimulateOffline() {
        messageSender.setSimulateOffline(!uiState.value.simulateOffline)
    }

    fun send() = viewModelScope.launch {
        val text = _input.value.trim()
        if (text.isBlank()) return@launch

        val queued = uiState.value.simulateOffline || !connectionManager.isConnectedNow()
        appendMessage(ChatMessageUi.Direction.OUT, text, queued = queued)
        _input.value = ""

        runCatching {
            messageSender.sendText(text = text, target = "default")
        }.onFailure { ex ->
            _error.value = ex.message ?: "send failed"
        }
    }

    private fun appendMessage(dir: ChatMessageUi.Direction, text: String, queued: Boolean) {
        val newItem = ChatMessageUi(
            id = "m-${System.nanoTime()}",
            direction = dir,
            text = text,
            timeMs = System.currentTimeMillis(),
            queued = queued,
            attempt = 0
        )
        _messages.value = _messages.value + newItem
    }
}
