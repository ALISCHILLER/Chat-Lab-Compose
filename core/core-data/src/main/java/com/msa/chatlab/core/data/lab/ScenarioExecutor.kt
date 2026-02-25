package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.domain.lab.RunEvent
import com.msa.chatlab.core.domain.lab.RunProgress
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.RunSession
import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.value.RunId
import com.msa.chatlab.core.domain.model.device.DeviceInfoProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ScenarioExecutor(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager,
    private val messageSender: MessageSender,
    private val deviceInfo: DeviceInfoProvider
) {

    data class RunBundle(
        val session: RunSession,
        val events: List<RunEvent>,
        val result: RunResult
    )

    private val _progress = MutableStateFlow(RunProgress())
    val progress = _progress.asStateFlow()

    suspend fun execute(scenario: Scenario): RunBundle {

        val profile = activeProfileStore.getActiveNow() ?: error("No active profile")
        val startedAt = System.currentTimeMillis()
        val session = RunSession(
            runId = RunId(UUID.randomUUID().toString()),
            scenario = scenario,
            seed = startedAt,
            startedAt = startedAt,
            networkLabel = profile.protocolType.name
        )

        val metrics = MetricsCalculator()
        val events = mutableListOf<RunEvent>()
        _progress.value = RunProgress(status = RunProgress.Status.Running, percent = 0)

        val total = (scenario.durationSec * scenario.rps).coerceAtLeast(1)
        val intervalMs = (1000L / scenario.rps.coerceAtLeast(1)).coerceAtLeast(1L)

        repeat(total) { i ->
            val messageId = UUID.randomUUID().toString()
            val payload = "x".repeat(scenario.payloadBytes.coerceAtLeast(1))
            messageSender.sendTextWithMessageId(payload, "echo", messageId)
            metrics.onEnqueued(messageId)
            events += RunEvent.Enqueued(com.msa.chatlab.core.domain.value.TimestampMillis(System.currentTimeMillis()), messageId)

            _progress.value = _progress.value.copy(
                percent = (((i + 1) * 100f) / total).toInt().coerceIn(0, 100),
                sentCount = (i + 1).toLong()
            )
            delay(intervalMs)
        }

        val endedAt = System.currentTimeMillis()
        _progress.value = _progress.value.copy(status = RunProgress.Status.Completed, percent = 100)
        return RunBundle(session, events, metrics.buildResult(session, endedAt))
    }
}
