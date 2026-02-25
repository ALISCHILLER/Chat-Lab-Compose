package com.msa.chatlab.core.data.lab

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
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.device.DeviceInfoProvider

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

        val profile = activeProfileStore.getActiveNow() ?: error("No active profile")
        val runId = RunId(UUID.randomUUID().toString())
        val session = RunSession(
            id = runId,
            profileId = profile.id,
            scenarioPreset = scenario.preset,
            protocolType = profile.protocolType.name,
            startedAt = nowTs()
        )
        currentRun = session

        val runResult = try {
            withTimeoutOrNull(scenario.timeoutSeconds * 1000L) {
                // 1. Wait for connection
                record(RunEvent.Connecting(t = nowTs()))
                updateProgress(0, "Connecting...")
                waitForConnection(scenario.connectionTimeoutSeconds * 1000L)
                record(RunEvent.Connected(t = nowTs()))

                // 2. Run steps
                scenario.steps.forEachIndexed { i, step ->
                    if (!isActive) throw CancellationException("Run cancelled by user")
                    val p = ((i + 1f) / scenario.steps.size * 100).roundToInt()
                    updateProgress(p, "Running step: ${step.name}")

                    when (step) {
                        is Scenario.Step.SendMessage -> {
                            record(RunEvent.SendMessage(t = nowTs(), messageId = step.messageId, payloadBytes = step.payloadBytes))
                            val payload = padPayload(step.payload, step.payloadBytes)
                            messageSender.sendTextWithMessageId(payload, "echo", step.messageId)
                        }
                        is Scenario.Step.Delay -> {
                            record(RunEvent.Delay(t = nowTs(), ms = step.ms))
                            delay(step.ms)
                        }
                    }
                }
                // 3. Wait for all messages to be acked
                updateProgress(100, "Waiting for acks...")
                waitForAcks(scenario.ackTimeoutSeconds * 1000L)
                record(RunEvent.AllAcksReceived(t = nowTs()))

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

        val finalResult = withContext(NonCancellable) {
            // 4. Teardown
            updateProgress(100, "Tearing down...")
            if (connectionManager.connectionState.value !is ConnectionState.Idle) {
                connectionManager.disconnect()
                waitForDisconnection(5_000)
                ensureDisconnectedEvent()
            }

            // 5. Finalize
            val result = if (runResult?.isSuccess == true) {
                metrics.calculateSuccess(events)
            } else {
                val errorMessage = when (val e = runResult?.exceptionOrNull()) {
                    is TimeoutException -> "Timeout: ${e.message}"
                    is CancellationException -> "Cancelled by user"
                    else -> e?.message ?: "Unknown error"
                }
                record(RunEvent.Error(t = nowTs(), message = errorMessage))
                metrics.calculateFailure(events, errorMessage)
            }

            RunBundle(session, events.toList(), result)
        }

        currentRun = null
        _progress.value = RunProgress(100, "Finished")
        return@coroutineScope finalResult
    }

    private suspend fun waitForConnection(timeout: Long) {
        withTimeoutOrNull(timeout) {
            connectionManager.connectionState.first { it is ConnectionState.Connected }
        } ?: throw TimeoutException("Connection timed out")
    }

    private suspend fun waitForAcks(timeout: Long) {
        // This part needs to be implemented. For now, it just waits.
        // In a real scenario, you'd check an AckTracker or similar.
        delay(1000) // Placeholder
    }

    private suspend fun waitForDisconnection(timeout: Long) {
        withTimeoutOrNull(timeout) {
            connectionManager.connectionState.first { it is ConnectionState.Idle || it is ConnectionState.Disconnected }
        }
    }

    private fun updateProgress(percent: Int, message: String) {
        val p = _progress.value
        _progress.value = p.copy(percent = percent, message = message)
    }

    private fun CoroutineScope.watchEvents() {
        launch {
            connectionManager.events.collect {
                if (currentRun == null) return@collect
                when (it) {
                    is TransportEvent.MessageReceived -> {
                        metrics.onMessageReceived(it.payload.envelope)
                        record(RunEvent.MessageReceived(t = nowTs(), messageId = it.payload.envelope.messageId.value, roundTripMs = 0))
                    }
                    is TransportEvent.MessageSent -> {
                        metrics.onMessageSent(it.messageId)
                    }
                    else -> Unit
                }
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
