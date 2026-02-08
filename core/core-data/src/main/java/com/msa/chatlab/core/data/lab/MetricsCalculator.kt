package com.msa.chatlab.core.data.lab

import kotlin.math.max

class MetricsCalculator {
    data class Counters(
        var sent: Long = 0,
        var received: Long = 0,
        var failed: Long = 0,
        var enqueued: Long = 0
    )

    private val sentAt = linkedMapOf<String, Long>()
    private val latencies = mutableListOf<Long>()
    val counters = Counters()

    fun onEnqueued(messageId: String) {
        counters.enqueued++
        // enqueue time ثبت نمی‌کنیم؛ latency فقط send->receive است
    }

    fun onSent(messageId: String, atMs: Long) {
        counters.sent++
        sentAt[messageId] = atMs
    }

    fun onReceived(messageId: String?, atMs: Long) {
        counters.received++
        if (messageId == null) return
        val s = sentAt.remove(messageId) ?: return
        latencies.add(atMs - s)
    }

    fun onFailed() { counters.failed++ }

    fun buildResult(durationMs: Long): RunResult {
        val sorted = latencies.sorted()
        fun pct(p: Double): Long? {
            if (sorted.isEmpty()) return null
            val idx = (sorted.size * p).toInt().coerceIn(0, sorted.lastIndex)
            return sorted[idx]
        }
        val throughput = (counters.sent.toDouble() / max(durationMs / 1000.0, 1.0))
        val successRate = if (counters.sent > 0) (counters.received.toDouble() / counters.sent) * 100 else 0.0

        return RunResult(
            sent = counters.sent,
            received = counters.received,
            failed = counters.failed,
            enqueued = counters.enqueued,
            latencyP50Ms = pct(0.50),
            latencyP95Ms = pct(0.95),
            latencyP99Ms = pct(0.99),
            throughputMsgPerSec = throughput,
            successRatePercent = successRate
        )
    }
}
