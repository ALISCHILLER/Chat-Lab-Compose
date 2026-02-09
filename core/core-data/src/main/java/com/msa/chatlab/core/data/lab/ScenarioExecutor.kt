package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.domain.value.RunId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.math.max

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

    val events = mutableListOf<RunEvent>()
    private val metrics = MetricsCalculator()

    private var collectorJob: Job? = null
    var currentRun: RunSession? = null

    suspend fun execute(scenario: Scenario): RunBundle {
        events.clear()
        metrics.counters.apply { sent = 0; received = 0; failed = 0; enqueued = 0 }

        val profile = activeProfileStore.getActiveNow()
            ?: error("No active profile selected")

        val startMs = System.currentTimeMillis()
        val runId = RunId("run-$startMs")

        currentRun = RunSession(
            runId = runId,
            scenario = scenario,
            profileName = profile.name,
            protocolType = profile.protocolType.name,
            startedAt = TimestampMillis(startMs),
            seed = scenario.seed,
            deviceModel = deviceInfo.deviceModel(),
            osVersion = deviceInfo.osVersion(),
            networkLabel = deviceInfo.networkLabel()
        )

        // آماده‌سازی اتصال
        connectionManager.prepareTransport()
        connectionManager.connect()

        startCollectingEvents()

        val chaos = ChaosEngine(scenario.seed)

        // Load generator: ارسال پیام
        val load = LoadGenerator(
            scope = scope,
            durationMs = scenario.durationMs,
            ratePerSecond = scenario.messageRatePerSecond,
            burstEvery = scenario.burstEvery,
            burstSize = scenario.burstSize
        ) { text ->
            // سناریوی lossy: drop/delay مصنوعی در سطح «آزمایشگاه»
            if (chaos.shouldDrop(scenario.dropRatePercent)) {
                metrics.onFailed()
                record(RunEvent.Failed(nowTs(), null, "lab_drop"))
                return@LoadGenerator
            }
            val extraDelay = chaos.extraDelayMs(scenario.minExtraDelayMs, scenario.maxExtraDelayMs)
            if (extraDelay > 0) delay(extraDelay)

            // ارسال واقعی
            messageSender.sendText(text, "default")
        }

        // کنترل قطع/وصل برنامه‌ریزی‌شده
        val toggler = scope.launch {
            val end = startMs + scenario.durationMs
            while (isActive && System.currentTimeMillis() < end) {
                val elapsed = System.currentTimeMillis() - startMs
                val mustDisconnect = chaos.isInDisconnectWindow(elapsed, scenario.disconnects)

                if (mustDisconnect && connectionManager.isConnectedNow()) {
                    connectionManager.disconnect()
                    record(RunEvent.Disconnected(nowTs(), "scheduled"))
                } else if (!mustDisconnect && !connectionManager.isConnectedNow()) {
                    connectionManager.connect()
                    record(RunEvent.Connected(nowTs()))
                }

                delay(100)
            }
        }

        load.start()

        // صبر تا پایان سناریو + کمی زمان برای flush
        delay(scenario.durationMs + 1500)

        load.stop()
        toggler.cancel()

        connectionManager.disconnect()
        collectorJob?.cancel()

        val result = metrics.buildResult(durationMs = scenario.durationMs)

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
                    is TransportEvent.Connected -> record(RunEvent.Connected(nowTs()))
                    is TransportEvent.Disconnected -> record(RunEvent.Disconnected(nowTs(), ev.reason ?: "unknown"))
                    is TransportEvent.MessageSent -> {
                        metrics.onSent(ev.messageId.value, System.currentTimeMillis())
                        record(RunEvent.Sent(nowTs(), ev.messageId.value))
                    }
                    is TransportEvent.MessageReceived -> {
                        metrics.onReceived(ev.payload.envelope.messageId.value, System.currentTimeMillis())
                        record(RunEvent.Received(nowTs(), ev.payload.envelope.messageId.value))
                    }
                    is TransportEvent.ErrorOccurred -> {
                        metrics.onFailed()
                        record(RunEvent.Failed(nowTs(), null, ev.error.message ?: "error"))
                    }
                }
            }
        }
    }

    private fun record(e: RunEvent) { events.add(e) }
    private fun nowTs() = TimestampMillis(System.currentTimeMillis())

    sealed class RunEvent {
        abstract val timestampMs: Long
        data class Connected(override val timestampMs: Long = 0) : RunEvent()
        data class Disconnected(val reason: String, override val timestampMs: Long = 0) : RunEvent()
        data class MessageSent(val messageId: String, override val timestampMs: Long = 0) : RunEvent()
        data class MessageReceived(override val timestampMs: Long = 0, val messageId: String? = null) : RunEvent()
        data class Error(val message: String, override val timestampMs: Long = 0) : RunEvent()
    }
}
