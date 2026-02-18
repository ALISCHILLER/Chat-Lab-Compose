package com.msa.chatlab.core.common.ui.messenger

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface UiEffect {
    data class Snackbar(val message: String, val actionLabel: String? = null) : UiEffect
}

interface UiMessenger {
    val effects: Flow<UiEffect>
    suspend fun emit(effect: UiEffect)
}

class ChannelUiMessenger : UiMessenger {
    private val ch = Channel<UiEffect>(capacity = Channel.BUFFERED)
    override val effects: Flow<UiEffect> = ch.receiveAsFlow()
    override suspend fun emit(effect: UiEffect) { ch.send(effect) }
}
