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
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScenarioExecutor(
    private val scope: CoroutineScope,
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

    private val events = mutableListOf<RunEvent>()
    private val metrics = MetricsCalculator()

    private var collectorJob: Job? = null
    var currentRun: RunSession? = null

    suspend fun execute(scenario: Scenario): RunBundle {
        _progress.value = RunProgress(status = RunProgress.Status.Running)
        events.clear()
        metrics.counters.apply { sent = 0; received = 0; failed = 0; enqueued = 0 }

        val profile = activeProfileStore.getActiveNow()
            ?: error("No active profile selected").also {
                _progress.value = RunProgress(status = RunProgress.Status.Failed, lastError = "No active profile")
            }

        val startMs = System.currentTimeMillis()
        val runId = RunId("run-$startMs")

        currentRun = RunSession(
            runId = runId,
            scenario = scenario,
            startedAt = startMs,
            seed = System.currentTimeMillis(),
            networkLabel = deviceInfo.networkLabel()
        )

        connectionManager.prepareTransport()
        connectionManager.connect()

        startCollectingEvents()

        val chaos = ChaosEngine(currentRun!!.seed)

        val load = LoadGenerator(
            scope = scope,
            durationMs = scenario.durationSec * 1000L,
            ratePerSecond = scenario.rps.toDouble(),
            burstEvery = 0,
            burstSize = 0
        ) { text ->
            messageSender.sendText(text, "default")
        }

        val progressUpdater = scope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startMs
                val percent = ((elapsed.toDouble() / (scenario.durationSec * 1000L)) * 100).toInt().coerceIn(0, 100)
                _progress.value = _progress.value.copy(
                    percent = percent,
                    elapsedMs = elapsed,
                    sentCount = metrics.counters.sent,
                    successCount = metrics.counters.received,
                    failCount = metrics.counters.failed
                )
                delay(200)
            }
        }

        val toggler = scope.launch { /* ... */ }
        load.start()
        delay(scenario.durationSec * 1000L + 1500)

        load.stop()
        toggler.cancel()
        progressUpdater.cancel()
        connectionManager.disconnect()
        collectorJob?.cancel()

        val result = metrics.buildResult(session = requireNotNull(currentRun), endedAt = System.currentTimeMillis())
        _progress.value = _progress.value.copy(status = RunProgress.Status.Completed, percent = 100)

        return RunBundle(
            session = requireNotNull(currentRun),
            events = events.toList(),
            result = result
        )
    }

    private fun startCollectingEvents() {
        collectorJob?.cancel()
        collectorJob = scope.launch {
            connectionManager.events.collect { ev ->
                when (ev) {
                    is TransportEvent.Connected -> record(RunEvent.Connected(t = nowTs()))
                    is TransportEvent.Disconnected -> record(RunEvent.Disconnected(t = nowTs(), reason = ev.reason ?: "unknown"))
                    is TransportEvent.MessageSent -> {
                        metrics.onSent(ev.messageId, nowTs().value)
                        record(RunEvent.Sent(t = nowTs(), messageId = ev.messageId))
                    }
                    is TransportEvent.MessageReceived -> {
                        metrics.onReceived(ev.payload.envelope?.messageId?.value, nowTs().value)
                        record(RunEvent.Received(t = nowTs(), messageId = ev.payload.envelope?.messageId?.value))
                    }
                    is TransportEvent.ErrorOccurred -> {
                        metrics.onFailed()
                        record(RunEvent.Failed(t = nowTs(), messageId = null, error = ev.error.message ?: "error"))
                    }
                }
            }
        }
    }

    private fun record(e: RunEvent) { events.add(e) }
    private fun nowTs() = TimestampMillis(System.currentTimeMillis())
}
