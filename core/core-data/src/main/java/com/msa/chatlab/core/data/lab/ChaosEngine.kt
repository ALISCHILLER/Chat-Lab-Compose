package com.msa.chatlab.core.data.lab

import kotlin.random.Random

class ChaosEngine(seed: Long) {
    private val random = Random(seed)

    fun shouldDrop(dropRatePercent: Double): Boolean {
        if (dropRatePercent <= 0) return false
        return random.nextDouble(0.0, 100.0) < dropRatePercent
    }

    fun extraDelayMs(min: Long, max: Long): Long {
        if (max <= min) return 0L
        return random.nextLong(min, max)
    }
}
