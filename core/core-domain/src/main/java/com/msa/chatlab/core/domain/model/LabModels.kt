package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.RunId

data class Scenario(
    val name: String,
    val durationSec: Int,
    val rps: Int, // requests per second
    val payloadBytes: Int,
    val pattern: String // e.g., "burst", "steady"
)

data class RunSession(
    val runId: RunId,
    val scenario: Scenario,
    val seed: Long,
    val startedAt: Long,
    val networkLabel: String,
    val enqueued: List<MessageId> = emptyList()
)

data class RunResult(
    val session: RunSession,
    val endedAt: Long,
    val sent: List<MessageId> = emptyList(),
    val failed: List<Pair<MessageId, String>> = emptyList(),
    val received: List<MessageId> = emptyList()
)
