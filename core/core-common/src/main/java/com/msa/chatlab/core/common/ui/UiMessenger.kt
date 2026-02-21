package com.msa.chatlab.core.common.ui

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface UiEffect {
    data class Snackbar(val message: String) : UiEffect
}

interface UiMessenger {
    val effects: SharedFlow<UiEffect>

    suspend fun emit(effect: UiEffect)

    fun tryEmit(effect: UiEffect): Boolean

    suspend fun snackbar(message: String) = emit(UiEffect.Snackbar(message))

    fun trySnackbar(message: String) = tryEmit(UiEffect.Snackbar(message))
}

class ChannelUiMessenger(
    extraBufferCapacity: Int = 32
) : UiMessenger {

    private val _effects = MutableSharedFlow<UiEffect>(
        replay = 0,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    override suspend fun emit(effect: UiEffect) {
        _effects.emit(effect)
    }

    override fun tryEmit(effect: UiEffect): Boolean {
        return _effects.tryEmit(effect)
    }
}
