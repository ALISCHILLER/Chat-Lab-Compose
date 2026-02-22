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
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.roundToInt

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

    private val events = mutableListOf<RunEvent>()
    private val metrics = MetricsCalculator()

    var currentRun: RunSession? = null

    suspend fun execute(scenario: Scenario): RunBundle = coroutineScope {
        metrics.reset()
        events.clear()
        connectionManager.setSimulateOffline(false)

        val profile = activeProfileStore.getActiveNow()
        if (profile == null) {
            _progress.value = RunProgress(status = RunProgress.Status.Failed, lastError = "No active profile selected")
            throw IllegalStateException("No active profile selected")
        }

        val startMs = System.currentTimeMillis()
        val runId = RunId("run-$startMs")
        val seed = startMs

        val session = RunSession(
            runId = runId,
            scenario = scenario,
            startedAt = startMs,
            seed = seed,
            networkLabel = deviceInfo.networkLabel()
        )
        currentRun = session

        _progress.value = RunProgress(status = RunProgress.Status.Running, percent = 0, elapsedMs = 0)

        connectionManager.prepareTransport()
        connectionManager.connect()

        val collectorJob = launch {
            connectionManager.events.collect { ev ->
                when (ev) {
                    is TransportEvent.Connected -> record(RunEvent.Connected(t = nowTs()))
                    is TransportEvent.Disconnected -> record(RunEvent.Disconnected(t = nowTs(), reason = ev.reason ?: "unknown"))

                    is TransportEvent.MessageSent -> {
                        metrics.onSent(ev.messageId, nowTs().value)
                        record(RunEvent.Sent(t = nowTs(), messageId = ev.messageId))
                    }

                    is TransportEvent.MessageReceived -> {
                        val mid = ev.payload.envelope.messageId.value
                        metrics.onReceived(mid, nowTs().value)
                        record(RunEvent.Received(t = nowTs(), messageId = mid))
                    }

                    is TransportEvent.ErrorOccurred -> {
                        metrics.onFailed()
                        record(RunEvent.Failed(t = nowTs(), messageId = null, error = ev.error.message ?: "error"))
                    }
                }
            }
        }

        val chaos = ChaosEngine(seed)
        val dropRatePercent = if (scenario.pattern == "lossy") 20.0 else 0.0
        val burstEvery = if (scenario.pattern == "burst") 20 else 0
        val burstSize = if (scenario.pattern == "burst") 10 else 0

        val progressJob = launch {
            val totalMs = (scenario.durationSec * 1000L).coerceAtLeast(1L)
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startMs
                val percent = ((elapsed.toDouble() / totalMs.toDouble()) * 100.0)
                    .roundToInt().coerceIn(0, 100)

                _progress.value = _progress.value.copy(
                    status = _progress.value.status,
                    percent = percent,
                    elapsedMs = elapsed,
                    sentCount = metrics.counters.sent,
                    successCount = metrics.counters.received,
                    failCount = metrics.counters.failed
                )
                delay(200)
            }
        }

        val togglerJob = launch { applyNetworkPattern(pattern = scenario.pattern) }

        val load = LoadGenerator(
            scope = this, // ✅ مهم: داخل همین coroutineScope
            durationMs = scenario.durationSec * 1000L,
            ratePerSecond = scenario.rps.toDouble(),
            burstEvery = burstEvery,
            burstSize = burstSize
        ) { baseText ->
            if (chaos.shouldDrop(dropRatePercent)) {
                metrics.onFailed()
                record(RunEvent.Failed(t = nowTs(), messageId = null, error = "Dropped by lossy profile"))
                return@LoadGenerator
            }

            val mid = UUID.randomUUID().toString()
            metrics.onEnqueued(mid)
            record(RunEvent.Enqueued(t = nowTs(), messageId = mid))

            val payload = padPayload(baseText, scenario.payloadBytes)
            messageSender.sendTextWithMessageId(payload, "default", mid)
        }

        try {
            load.start()
            delay(scenario.durationSec * 1000L)
            delay(1500)

            val endedAt = System.currentTimeMillis()
            val result = metrics.buildResult(session = session, endedAt = endedAt)

            _progress.value = _progress.value.copy(status = RunProgress.Status.Completed, percent = 100)

            RunBundle(session, events.toList(), result)
        } catch (ce: CancellationException) {
            _progress.value = _progress.value.copy(status = RunProgress.Status.Cancelled, lastError = "Cancelled")
            throw ce
        } catch (e: Exception) {
            _progress.value = _progress.value.copy(status = RunProgress.Status.Failed, lastError = e.message)
            throw e
        } finally {
            withContext(NonCancellable) {
                if (_progress.value.status == RunProgress.Status.Running) {
                    _progress.value = _progress.value.copy(status = RunProgress.Status.Stopping)
                }

                runCatching { load.stop() }
                togglerJob.cancel()
                progressJob.cancel()

                connectionManager.setSimulateOffline(false)

                runCatching { connectionManager.disconnect() }
                waitForDisconnected(timeoutMs = 1200)
                ensureDisconnectedEvent()

                collectorJob.cancel()
            }
        }
    }

    private suspend fun applyNetworkPattern(pattern: String) {
        when (pattern) {
            "offline_burst" -> {
                connectionManager.setSimulateOffline(true)
                delay(8_000)
                connectionManager.setSimulateOffline(false)
            }
            "intermittent" -> {
                while (kotlin.coroutines.coroutineContext.isActive) {
                    connectionManager.setSimulateOffline(true)
                    delay(2_000)
                    connectionManager.setSimulateOffline(false)
                    delay(5_000)
                }
            }
            else -> connectionManager.setSimulateOffline(false)
        }
    }

    private suspend fun waitForDisconnected(timeoutMs: Long) {
        withTimeoutOrNull(timeoutMs) {
            while (kotlin.coroutines.coroutineContext.isActive) {
                val st = connectionManager.connectionState.value
                if (st is ConnectionState.Disconnected || st is ConnectionState.Idle) return@withTimeoutOrNull
                delay(50)
            }
        }
    }

    private fun ensureDisconnectedEvent() {
        if (events.none { it is RunEvent.Disconnected }) {
            record(RunEvent.Disconnected(t = nowTs(), reason = "forced_disconnect_timeout"))
        }
    }

    private fun padPayload(base: String, payloadBytes: Int): String {
        if (payloadBytes <= 0) return base
        val bytes = base.encodeToByteArray().size
        if (bytes >= payloadBytes) return base
        val pad = payloadBytes - bytes
        return buildString {
            append(base)
            append(' ')
            repeat(pad) { append('x') }
        }
    }

    private fun record(e: RunEvent) { events.add(e) }
    private fun nowTs() = TimestampMillis(System.currentTimeMillis())
}