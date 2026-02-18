package com.msa.chatlab.core.common.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface UiEffect {
    data class Snackbar(
        val message: String,
        val actionLabel: String? = null
    ) : UiEffect
}

interface UiMessenger {
    val effects: SharedFlow<UiEffect>
    fun emit(effect: UiEffect)
}

class UiMessengerImpl : UiMessenger {
    private val _effects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 64)
    override val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    override fun emit(effect: UiEffect) {
        _effects.tryEmit(effect)
    }
}
