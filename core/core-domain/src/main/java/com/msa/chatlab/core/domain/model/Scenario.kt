package com.msa.chatlab.core.domain.model

data class Scenario(
    val preset: ScenarioPreset = ScenarioPreset.Stable,
    val durationMs: Long = 60_000,
    val messageRatePerSecond: Double = 3.0,
    val burstSize: Int = 0,
    val seed: Long = 42,
    val customDisconnects: List<DisconnectWindow> = emptyList()
)

enum class ScenarioPreset {
    Stable,
    Intermittent,
    OfflineBurst,
    Lossy,
    LoadBurst,
    Custom
}
