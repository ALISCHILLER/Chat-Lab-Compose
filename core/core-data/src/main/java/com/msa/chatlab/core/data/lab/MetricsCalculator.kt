package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession
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

    fun buildResult(session: RunSession, endedAt: Long): RunResult {
        return RunResult(
            session = session,
            endedAt = endedAt,
            sent = sentAt.keys.map { com.msa.chatlab.core.domain.value.MessageId(it) },
            failed = emptyList(),
            received = emptyList()
        )
    }
}
