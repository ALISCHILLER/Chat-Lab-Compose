package com.msa.chatlab.core.domain.model

data class OutboxPolicy(
    val enabled: Boolean = true,
    val maxQueueSize: Int = 1_000,
    val persistToDisk: Boolean = true
)

data class RetryPolicy(
    val maxAttempts: Int = 5,
    val initialBackoffMs: Long = 500,
    val maxBackoffMs: Long = 30_000,
    val jitterRatio: Double = 0.2
)

data class ReconnectPolicy(
    val enabled: Boolean = true,
    val maxAttempts: Int = Int.MAX_VALUE,
    val backoffMs: Long = 2_000
)

sealed interface AckStrategy {
    data object None : AckStrategy
    data object TransportLevel : AckStrategy
    data class ApplicationLevel(val ackTimeoutMs: Long = 5_000) : AckStrategy
}
