package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.RunId

data class Scenario(
    val name: String,
    val durationSec: Int,
    val rps: Int,
    val payloadBytes: Int,
    val pattern: String
)

data class RunSession(
    val runId: RunId,
    val scenario: Scenario,
    val seed: Long,
    val startedAt: Long,
    val networkLabel: String,
)

data class RunResult(
    val session: RunSession,
    val endedAt: Long,
    val enqueuedCount: Long,
    val sentCount: Long,
    val receivedCount: Long,
    val failedCount: Long,
    val successRatePercent: Double,
    val throughputMsgPerSec: Double,
    val latencyAvgMs: Long?,
    val latencyP50Ms: Long?,
    val latencyP95Ms: Long?,
    val latencyP99Ms: Long?,
)