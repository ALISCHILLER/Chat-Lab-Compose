package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession
import kotlin.math.roundToLong

class MetricsCalculator {

    data class Counters(
        var enqueued: Long = 0,
        var sent: Long = 0,
        var received: Long = 0,
        var failed: Long = 0,
    )

    val counters = Counters()
    private val sentAt = linkedMapOf<String, Long>()
    private val latenciesMs = mutableListOf<Long>()

    fun reset() {
        counters.enqueued = 0
        counters.sent = 0
        counters.received = 0
        counters.failed = 0
        sentAt.clear()
        latenciesMs.clear()
    }

    fun onEnqueued(messageId: String) { counters.enqueued++ }

    fun onSent(messageId: String, atMs: Long) {
        counters.sent++
        sentAt[messageId] = atMs
    }

    fun onReceived(messageId: String?, atMs: Long) {
        counters.received++
        if (messageId == null) return
        val s = sentAt.remove(messageId) ?: return
        latenciesMs += (atMs - s).coerceAtLeast(0)
    }

    fun onFailed() { counters.failed++ }

    fun buildResult(session: RunSession, endedAt: Long): RunResult {
        val durationSec = ((endedAt - session.startedAt).coerceAtLeast(1) / 1000.0)
        val sent = counters.sent
        val received = counters.received
        val failed = counters.failed
        val successRate = if (sent <= 0) 0.0 else (received.toDouble() / sent.toDouble()) * 100.0
        val throughput = if (durationSec <= 0.0) 0.0 else received / durationSec

        val sorted = latenciesMs.sorted()
        val avg = if (sorted.isEmpty()) null else (sorted.average().roundToLong())

        return RunResult(
            session = session,
            endedAt = endedAt,
            enqueuedCount = counters.enqueued,
            sentCount = sent,
            receivedCount = received,
            failedCount = failed,
            successRatePercent = successRate,
            throughputMsgPerSec = throughput,
            latencyAvgMs = avg,
            latencyP50Ms = percentile(sorted, 0.50),
            latencyP95Ms = percentile(sorted, 0.95),
            latencyP99Ms = percentile(sorted, 0.99),
        )
    }

    private fun percentile(sorted: List<Long>, p: Double): Long? {
        if (sorted.isEmpty()) return null
        val clamped = p.coerceIn(0.0, 1.0)
        val idx = ((sorted.size - 1) * clamped).roundToLong().toInt().coerceIn(0, sorted.size - 1)
        return sorted[idx]
    }
}