package com.msa.chatlab.core.data.outbox

import kotlin.math.min

data class RetryConfig(
    val maxAttempts: Int = 5,
    val baseDelayMs: Long = 500,
    val maxDelayMs: Long = 10_000
)

object RetryPolicyEngine {

    fun canRetry(attempt: Int, cfg: RetryConfig): Boolean {
        // attempt یعنی attempt فعلی، مثلا attempt=0 یعنی اولین بار هنوز نزده
        return (attempt + 1) < cfg.maxAttempts
    }

    fun nextDelayMs(attempt: Int, cfg: RetryConfig): Long {
        // exponential ساده: 500, 1000, 2000, 4000...
        val exp = cfg.baseDelayMs * (1L shl min(attempt, 10))
        return min(exp, cfg.maxDelayMs)
    }
}
