package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.lab.RunEvent
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession

class RunExporter {

    fun profileUsedJson(profile: Profile): String {
        return """
        {
          "profile_name": "${profile.name}",
          "protocol_type": "${profile.protocolType}"
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
          "scenario": "${session.scenario.name}",
          "seed": ${session.seed},
          "started_at": ${session.startedAt},
          "sent": ${result.sent.size},
          "received": ${result.received.size},
          "failed": ${result.failed.size},
          "enqueued": ${session.enqueued.size}
        }
        """.trimIndent()
    }

    private fun escapeCsv(s: String): String =
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) "\"${s.replace("\"", "\"\"")}\"" else s
}
