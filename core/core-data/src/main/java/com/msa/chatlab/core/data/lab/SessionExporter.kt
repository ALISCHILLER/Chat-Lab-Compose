package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.lab.RunEvent
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession

class SessionExporter(
    private val profileManager: ProfileManager,
    private val codec: ProfileJsonCodec
) {
    fun exportRun(
        runSession: RunSession,
        runResult: RunResult,
        events: List<RunEvent>
    ): Map<String, String> {
        val profile = profileManager.activeStore.getActiveNow()
        val profileJson = profile?.let { codec.encode(it) } ?: "{}"

        val csv = buildString {
            appendLine("timestamp,event_type,message_id,reason")
            events.forEach { ev ->
                val (type, mid, reason) = when (ev) {
                    is RunEvent.Connected -> Triple("connected", "", "")
                    is RunEvent.Disconnected -> Triple("disconnected", "", ev.reason)
                    is RunEvent.Enqueued -> Triple("enqueued", ev.messageId, "")
                    is RunEvent.Sent -> Triple("sent", ev.messageId, "")
                    is RunEvent.Received -> Triple("received", ev.messageId ?: "", "")
                    is RunEvent.Failed -> Triple("failed", ev.messageId ?: "", ev.error)
                }
                appendLine("${ev.t.value},$type,$mid,$reason")
            }
        }

        val summary = """
        {
          "run_id": "${runSession.runId.value}",
          "network": "${runSession.networkLabel}",
          "scenario": {
            "name": "${runSession.scenario.name}",
            "durationSec": ${runSession.scenario.durationSec},
            "rps": ${runSession.scenario.rps},
            "payloadBytes": ${runSession.scenario.payloadBytes},
            "pattern": "${runSession.scenario.pattern}"
          },
          "profile": {
            "name": "${profile?.name ?: "unknown"}",
            "protocol": "${profile?.protocolType?.name ?: "unknown"}",
            "endpoint": "${profile?.transportConfig?.endpoint ?: "unknown"}"
          },
          "result": {
            "enqueued": ${runResult.enqueuedCount},
            "sent": ${runResult.sentCount},
            "received": ${runResult.receivedCount},
            "failed": ${runResult.failedCount},
            "successRatePercent": ${"%.4f".format(runResult.successRatePercent)},
            "throughputMsgPerSec": ${"%.4f".format(runResult.throughputMsgPerSec)},
            "latencyAvgMs": ${runResult.latencyAvgMs ?: "null"},
            "latencyP50Ms": ${runResult.latencyP50Ms ?: "null"},
            "latencyP95Ms": ${runResult.latencyP95Ms ?: "null"},
            "latencyP99Ms": ${runResult.latencyP99Ms ?: "null"}
          }
        }
        """.trimIndent()

        return mapOf(
            "profile_used.json" to profileJson,
            "run_events.csv" to csv,
            "metrics_summary.json" to summary
        )
    }
}