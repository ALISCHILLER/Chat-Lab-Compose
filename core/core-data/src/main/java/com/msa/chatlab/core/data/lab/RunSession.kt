package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.value.RunId
import com.msa.chatlab.core.domain.value.TimestampMillis

data class RunSession(
    val runId: RunId,
    val scenario: Scenario,
    val profileName: String,
    val protocolType: String,
    val startedAt: TimestampMillis,
    val seed: Long,
    val deviceModel: String,
    val osVersion: String,
    val networkLabel: String
)
