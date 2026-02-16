package com.msa.chatlab.feature.settings.state

sealed interface SettingsUiEffect {
    data class Toast(val message: String) : SettingsUiEffect
}
