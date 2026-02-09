package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import java.io.File

class SessionExporter(
    private val profileManager: ProfileManager,
    private val codec: ProfileJsonCodec
) {
    fun exportRun(
        runSession: RunSession,
        runResult: RunResult,
        events: List<ScenarioExecutor.RunEvent>,
        outputDir: File
    ): ExportBundle {
        outputDir.mkdirs()

        // 1. profile_used.json
        val profile = profileManager.getProfile(runSession.runId.value.let { com.msa.chatlab.core.domain.value.ProfileId(it) })
        val profileJson = profile?.let { codec.encode(it) } ?: "{}"
        File(outputDir, "profile_used.json").writeText(profileJson)

        // 2. run_events.csv
        val csv = buildString {
            appendLine("timestamp,event_type,message_id,reason")
            events.forEach { ev ->
                val (eventType, messageId, reason) = when (ev) {
                    is ScenarioExecutor.RunEvent.Connected -> Triple("connected", "", "")
                    is ScenarioExecutor.RunEvent.Disconnected -> Triple("disconnected", "", ev.reason)
                    is ScenarioExecutor.RunEvent.MessageSent -> Triple("sent", ev.messageId, "")
                    is ScenarioExecutor.RunEvent.MessageReceived -> Triple("received", "", "")
                    is ScenarioExecutor.RunEvent.Error -> Triple("error", "", ev.message)
                }
                appendLine("${ev.timestampMs},$eventType,$messageId,$reason")
            }
        }
        File(outputDir, "run_events.csv").writeText(csv)

        // 3. metrics_summary.json
        val summary = """
        {
          "run_id": "${runSession.runId.value}",
          "profile_name": "${runSession.profileName}",
          "protocol_type": "${runSession.protocolType}",
          "scenario_preset": "${runSession.scenarioPreset}",
          "started_at": ${runSession.startedAt.value},
          "sent": ${runResult.sent},
          "received": ${runResult.received},
          "failed": ${runResult.failed},
          "retried": ${runResult.retried},
          "latency_p50_ms": ${runResult.latencyP50Ms ?: "null"},
          "latency_p95_ms": ${runResult.latencyP95Ms ?: "null"},
          "latency_p99_ms": ${runResult.latencyP99Ms ?: "null"},
          "throughput_msg_per_sec": ${runResult.throughputMsgPerSec ?: "null"},
          "success_rate_percent": ${runResult.successRatePercent ?: "null"}
        }
        """.trimIndent()
        File(outputDir, "metrics_summary.json").writeText(summary)

        // 4. README.md
        val readme = """
        # ChatLab Run Report

        - **Run ID**: ${runSession.runId.value}
        - **Profile**: ${runSession.profileName}
        - **Protocol**: ${runSession.protocolType}
        - **Scenario**: ${runSession.scenarioPreset}
        - **Timestamp**: ${runSession.startedAt.value}

        ## Summary

        ${runResult.toSummary()}
        """.trimIndent()
        File(outputDir, "README.md").writeText(readme)

        return ExportBundle(
            profileUsedFile = File(outputDir, "profile_used.json"),
            eventsFile = File(outputDir, "run_events.csv"),
            metricsFile = File(outputDir, "metrics_summary.json"),
            readmeFile = File(outputDir, "README.md")
        )
    }

    data class ExportBundle(
        val profileUsedFile: File,
        val eventsFile: File,
        val metricsFile: File,
        val readmeFile: File
    )
}
