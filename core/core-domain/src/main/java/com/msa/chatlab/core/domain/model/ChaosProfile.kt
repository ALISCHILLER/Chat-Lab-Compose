package com.msa.chatlab.core.domain.model

data class ChaosProfile(
    val enabled: Boolean = false,
    val dropRatePercent: Double = 0.0,          // 0..100
    val delayMinMs: Long = 0,
    val delayMaxMs: Long = 0,
    val jitterMs: Long = 0,
    val disconnectSchedule: List<DisconnectWindow> = emptyList(),
    val seed: Long = 42
)

data class DisconnectWindow(
    val atMsFromStart: Long,
    val durationMs: Long
)
