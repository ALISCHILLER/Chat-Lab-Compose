package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.lab.RunEvent
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession

class RunExporter {

    fun profileUsedJson(session: RunSession): String {
        // اینجا اگر ProfileJsonCodec داری، بهتره codec.encode(profile) را ذخیره کنی.
        // فعلاً مینیمال و استاندارد:
        return """
        {
          "profile_name": "${session.profileName}",
          "protocol_type": "${session.protocolType}"
        }
        """.trimIndent()
    }

    fun eventsCsv(events: List<RunEvent>): String {
        val sb = StringBuilder()
        sb.appendLine("timestamp_ms,event_type,message_id,reason")
        for (e in events) {
            when (e) {
                is RunEvent.Connected ->
                    sb.appendLine("${e.t.value},connected,,")
                is RunEvent.Disconnected ->
                    sb.appendLine("${e.t.value},disconnected,,${escapeCsv(e.reason)}")
                is RunEvent.Enqueued ->
                    sb.appendLine("${e.t.value},enqueued,${e.messageId},")
                is RunEvent.Sent ->
                    sb.appendLine("${e.t.value},sent,${e.messageId},")
                is RunEvent.Received ->
                    sb.appendLine("${e.t.value},received,${e.messageId ?: ""},")
                is RunEvent.Failed ->
                    sb.appendLine("${e.t.value},failed,${e.messageId ?: ""},${escapeCsv(e.error)}")
            }
        }
        return sb.toString()
    }

    fun metricsSummaryJson(session: RunSession, result: RunResult): String {
        return """
        {
          "run_id": "${session.runId.value}",
          "scenario": "${session.scenario.preset.name}",
          "seed": ${session.seed},
          "started_at": ${session.startedAt.value},
          "device_model": "${session.deviceModel}",
          "os_version": "${session.osVersion}",
          "network": "${session.networkLabel}",
          "sent": ${result.sent},
          "received": ${result.received},
          "failed": ${result.failed},
          "enqueued": ${result.enqueued},
          "latency_p50_ms": ${result.latencyP50Ms ?: "null"},
          "latency_p95_ms": ${result.latencyP95Ms ?: "null"},
          "latency_p99_ms": ${result.latencyP99Ms ?: "null"},
          "throughput_msg_per_sec": ${result.throughputMsgPerSec ?: "null"},
          "success_rate_percent": ${result.successRatePercent ?: "null"}
        }
        """.trimIndent()
    }

    private fun escapeCsv(s: String): String =
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) "\"${s.replace("\"", "\"\"")}\"" else s
}
