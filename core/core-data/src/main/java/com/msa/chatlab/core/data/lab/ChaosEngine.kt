package com.msa.chatlab.core.data.lab

import kotlin.random.Random

class ChaosEngine(seed: Long) {
    private val r = Random(seed)

    fun shouldDrop(dropRatePercent: Double): Boolean =
        dropRatePercent > 0 && (r.nextDouble() * 100.0) < dropRatePercent

    fun extraDelayMs(minMs: Long, maxMs: Long): Long {
        if (maxMs <= 0 || maxMs <= minMs) return 0
        return minMs + (r.nextDouble() * (maxMs - minMs)).toLong()
    }

    fun isInDisconnectWindow(elapsedMs: Long, windows: List<Scenario.DisconnectWindow>): Boolean =
        windows.any { w -> elapsedMs in w.atMsFromStart until (w.atMsFromStart + w.durationMs) }
}
