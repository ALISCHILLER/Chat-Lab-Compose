package com.msa.chatlab.featuresettings.state

sealed interface SettingsUiEffect {
    data class Toast(val message: String) : SettingsUiEffect
}
