package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.model.Scenario
import kotlin.random.Random

class ChaosEngine(seed: Long) {
    private val r = Random(seed)

    fun shouldDrop(dropRatePercent: Double): Boolean =
        dropRatePercent > 0 && (r.nextDouble() * 100.0) < dropRatePercent

    fun extraDelayMs(minMs: Long, maxMs: Long): Long {
        if (maxMs <= 0 || maxMs <= minMs) return 0
        return minMs + (r.nextDouble() * (maxMs - minMs)).toLong()
    }

    fun isInDisconnectWindow(elapsedMs: Long, scenario: Scenario): Boolean = false
}
