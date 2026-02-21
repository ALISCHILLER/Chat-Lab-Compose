package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.lab.RunEvent
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
        val profile = runBlocking { profileManager.activeStore.getActiveNow() }
        val profileJson = profile?.let { codec.encode(it) } ?: "{}"

        val csv = buildString {
            appendLine("timestamp,event_type,message_id,reason")
            events.forEach { ev ->
                val (eventType, messageId, reason) = when (ev) {
                    is RunEvent.Connected -> Triple("connected", "", "")
                    is RunEvent.Disconnected -> Triple("disconnected", "", ev.reason)
                    is RunEvent.Enqueued -> Triple("enqueued", ev.messageId, "")
                    is RunEvent.Sent -> Triple("sent", ev.messageId, "")
                    is RunEvent.Received -> Triple("received", ev.messageId ?: "", "")
                    is RunEvent.Failed -> Triple("failed", ev.messageId ?: "", ev.error)
                }
                appendLine("${ev.t.value},$eventType,$messageId,$reason")
            }
        }

        val summary = """
        {
          "run_id": "${runSession.runId.value}",
          "profile_name": "${profile?.name ?: "unknown"}",
          "protocol_type": "${profile?.protocolType?.name ?: "unknown"}",
          "scenario_name": "${runSession.scenario.name}",
          "started_at": ${runSession.startedAt},
          "sent": ${runResult.sent.size},
          "received": ${runResult.received.size},
          "failed": ${runResult.failed.size},
          "enqueued": ${runSession.enqueued.size}
        }
        """.trimIndent()

        return mapOf(
            "profile_used.json" to profileJson,
            "run_events.csv" to csv,
            "metrics_summary.json" to summary
        )
    }
}
