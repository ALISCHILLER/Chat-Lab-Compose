package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.RunId

data class RunResult(
    val runId: RunId,
    val protocolType: ProtocolType,
    val sent: Long,
    val received: Long,
    val failed: Long,
    val retried: Long,
    val latencyP50Ms: Long? = null,
    val latencyP95Ms: Long? = null,
    val latencyP99Ms: Long? = null,
    val throughputMsgPerSec: Double? = null,
    val successRatePercent: Double? = null
)
