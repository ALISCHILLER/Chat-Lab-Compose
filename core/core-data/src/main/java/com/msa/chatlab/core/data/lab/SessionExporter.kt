package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.lab.RunEvent
import com.msa.chatlab.core.domain.lab.timestampMs
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession
import kotlinx.coroutines.runBlocking

class SessionExporter(
    private val profileManager: ProfileManager,
    private val codec: ProfileJsonCodec
) {
    fun exportRun(
        runSession: RunSession,
        runResult: RunResult,
        events: List<RunEvent>
    ): Map<String, String> {
        val profileJson = runBlocking { profileManager.getProfile(runSession.profileId)?.let { codec.encode(it) } ?: "{}" }

        val csv = buildString {
            appendLine("timestamp,event_type,message_id,reason")
            events.forEach { ev ->
                val (eventType, messageId, reason) = when (ev) {
                    is RunEvent.Connected -> Triple("connected", "", "")
                    is RunEvent.Disconnected -> Triple("disconnected", "", ev.reason)
                    is RunEvent.Sent -> Triple("sent", ev.messageId, "")
                    is RunEvent.Received -> Triple("received", ev.messageId, "")
                    is RunEvent.Failed -> Triple("failed", ev.messageId ?: "", ev.error)
                }
                appendLine("${ev.timestampMs},$eventType,$messageId,$reason")
            }
        }

        val summary = """
        {
          "run_id": "${runSession.runId.value}",
          "profile_name": "${runSession.profileName}",
          "protocol_type": "${runSession.protocolType}",
          "scenario_preset": "${runSession.scenario.preset.name}",
          "started_at": ${runSession.startedAt.value},
          "sent": ${runResult.sent},
          "received": ${runResult.received},
          "failed": ${runResult.failed},
          "enqueued": ${runResult.enqueued},
          "latency_p50_ms": ${runResult.latencyP50Ms ?: "null"},
          "latency_p95_ms": ${runResult.latencyP95Ms ?: "null"},
          "latency_p99_ms": ${runResult.latencyP99Ms ?: "null"},
          "throughput_msg_per_sec": ${runResult.throughputMsgPerSec ?: "null"},
          "success_rate_percent": ${runResult.successRatePercent ?: "null"}
        }
        """.trimIndent()

        return mapOf(
            "profile_used.json" to profileJson,
            "run_events.csv" to csv,
            "metrics_summary.json" to summary
        )
    }
}
