package com.msa.chatlab.feature.settings.state

import com.msa.chatlab.core.domain.model.ProtocolType

data class ProfileEditorUi(
    val id: String?,
    val name: String,
    val protocol: String,
    val endpoint: String,
    val destinationDefault: String,
    val retryMaxAttempts: Int,
    val retryInitialMs: Long,
    val retryMaxMs: Long,
    val chaosEnabled: Boolean,
    val chaosDropPercent: Double,
    val chaosDelayMinMs: Long,
    val chaosDelayMaxMs: Long
)
