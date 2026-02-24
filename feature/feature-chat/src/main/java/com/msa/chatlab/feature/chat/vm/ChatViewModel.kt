package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.common.ui.UiMessenger
import com.msa.chatlab.feature.chat.domain.usecase.GetChatUiStateUseCase
import com.msa.chatlab.feature.chat.domain.usecase.SendMessageUseCase
import com.msa.chatlab.feature.chat.state.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    getChatUiStateUseCase: GetChatUiStateUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    @Suppress("unused") private val uiMessenger: UiMessenger,
) : ViewModel() {

    private val errorFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUiState> = getChatUiStateUseCase(errorFlow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun send(text: String, destination: String) {
        viewModelScope.launch {
            sendMessageUseCase(text, destination)
                .onFailure { errorFlow.value = it.message ?: "Send failed" }
        }
    }

    fun clearError() {
        errorFlow.value = null
    }
}
