package com.msa.chatlab.core.common.util

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

object Backoff {

    fun fixed(baseMs: Long, jitterRatio: Double = 0.2): Long {
        return addJitter(baseMs.coerceAtLeast(0), jitterRatio)
    }

    fun exponential(
        attempt: Int,
        initialMs: Long,
        maxMs: Long,
        jitterRatio: Double = 0.2
    ): Long {
        val a = attempt.coerceAtLeast(1)
        val raw = initialMs.toDouble() * (2.0.pow((a - 1).toDouble()))
        val capped = min(raw, maxMs.toDouble()).toLong().coerceAtLeast(0)
        return addJitter(capped, jitterRatio)
    }

    private fun addJitter(valueMs: Long, jitterRatio: Double): Long {
        if (valueMs <= 0) return 0
        val jr = jitterRatio.coerceIn(0.0, 1.0)
        val jitter = (valueMs * jr * (2 * Random.nextDouble() - 1)).toLong()
        return (valueMs + jitter).coerceAtLeast(0)
    }
}