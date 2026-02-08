package com.msa.chatlab.core.data.outbox

import kotlin.math.min

data class RetryConfig(
    val maxAttempts: Int = 5,
    val baseDelayMs: Long = 500,
    val maxDelayMs: Long = 10_000
)

object RetryEngine {
    fun canRetry(attempt: Int, cfg: RetryConfig): Boolean = (attempt + 1) < cfg.maxAttempts

    fun nextDelayMs(attempt: Int, cfg: RetryConfig): Long {
        val exp = cfg.baseDelayMs * (1L shl min(attempt, 10))
        return min(exp, cfg.maxDelayMs)
    }
}
