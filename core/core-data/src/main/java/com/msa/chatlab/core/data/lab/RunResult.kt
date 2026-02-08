package com.msa.chatlab.core.data.lab

data class RunResult(
    val sent: Long,
    val received: Long,
    val failed: Long,
    val enqueued: Long,
    val latencyP50Ms: Long?,
    val latencyP95Ms: Long?,
    val latencyP99Ms: Long?,
    val throughputMsgPerSec: Double?,
    val successRatePercent: Double?
)
