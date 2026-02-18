package com.msa.chatlab.feature.settings.state

data class ProfileEditorUi(
    val id: String? = null,
    val name: String = "",
    val protocol: String = "WebSocket",
    val endpoint: String = "",
    val destinationDefault: String = "default",
    val retryMaxAttempts: Int = 6,
    val retryInitialMs: Long = 400,
    val retryMaxMs: Long = 10_000,
    val chaosEnabled: Boolean = false,
    val chaosDropPercent: Double = 0.0,
    val chaosDelayMinMs: Long = 0,
    val chaosDelayMaxMs: Long = 0,
)

data class ImportExportUi(
    val json: String = "",
    val error: String? = null,
)
