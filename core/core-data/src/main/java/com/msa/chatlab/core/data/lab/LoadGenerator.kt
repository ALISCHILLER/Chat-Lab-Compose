package com.msa.chatlab.core.data.lab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LoadGenerator(
    private val scope: CoroutineScope,
    private val durationMs: Long,
    private val ratePerSecond: Double,
    private val burstEvery: Int,
    private val burstSize: Int,
    private val send: suspend (String) -> Unit
) {
    private var job: Job? = null
    private var counter = 0L

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            val start = System.currentTimeMillis()
            val end = start + durationMs
            val intervalMs = if (ratePerSecond <= 0) 1000 else (1000.0 / ratePerSecond).toLong()

            while (isActive && System.currentTimeMillis() < end) {
                val i = counter
                val elapsed = System.currentTimeMillis() - start

                if (burstEvery > 0 && burstSize > 0 && (i % burstEvery == 0L)) {
                    repeat(burstSize) { b ->
                        send("msg-$i-$elapsed-b$b")
                        counter++
                    }
                    delay(intervalMs.coerceAtLeast(1))
                } else {
                    send("msg-$i-$elapsed")
                    counter++
                    delay(intervalMs.coerceAtLeast(1))
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun sentSoFar(): Long = counter
}
