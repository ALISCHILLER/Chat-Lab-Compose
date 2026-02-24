package com.msa.chatlab.feature.lab.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.lab.ScenarioExecutor
import com.msa.chatlab.core.data.lab.SessionExporter
import com.msa.chatlab.core.domain.lab.RunProgress
import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.model.ScenarioPreset
import com.msa.chatlab.feature.lab.mapper.toDataScenario
import com.msa.chatlab.feature.lab.state.LabUiEffect
import com.msa.chatlab.feature.lab.state.LabUiEvent
import com.msa.chatlab.feature.lab.state.LabUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LabViewModel(
    private val scenarioExecutor: ScenarioExecutor,
    private val sessionExporter: SessionExporter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LabUiEffect>(extraBufferCapacity = 16)
    val uiEffect = _uiEffect.asSharedFlow()

    private var executionJob: Job? = null

    init {
        viewModelScope.launch {
            scenarioExecutor.progress.collectLatest { p ->
                _uiState.update { it.copy(progress = p) }
            }
        }
    }

    fun onEvent(event: LabUiEvent) {
        when (event) {
            is LabUiEvent.Start -> startScenarioDirect(event.scenario)

            LabUiEvent.StartStable -> startScenario(ScenarioPreset.Stable)
            LabUiEvent.StartIntermittent -> startScenario(ScenarioPreset.Intermittent)
            LabUiEvent.StartOfflineBurst -> startScenario(ScenarioPreset.OfflineBurst)
            LabUiEvent.StartLossy -> startScenario(ScenarioPreset.Lossy)
            LabUiEvent.StartLoadBurst -> startScenario(ScenarioPreset.LoadBurst)

            // ✅ Stop flow
            LabUiEvent.StopPressed, LabUiEvent.Stop -> requestStop()
            LabUiEvent.ConfirmStop -> confirmStop()
            LabUiEvent.DismissStopConfirm -> _uiState.update { it.copy(showStopConfirm = false) }

            LabUiEvent.RetryLast -> retryLast()
            LabUiEvent.CopyLastRunSummary -> copyLastRunSummary()

            LabUiEvent.ClearResults -> _uiState.update {
                it.copy(runResult = null, errorMessage = null, pastResults = emptyList(), activeScenario = null)
            }
        }
    }

    private fun startScenario(preset: ScenarioPreset) {
        val st = _uiState.value.progress.status
        if (st == RunProgress.Status.Running || st == RunProgress.Status.Stopping) return

        val scenario = toDataScenario(preset)
        _uiState.update { it.copy(lastPreset = preset) }
        startScenarioDirect(scenario)
    }

    private fun startScenarioDirect(scenario: Scenario) {
        val st = _uiState.value.progress.status
        if (st == RunProgress.Status.Running || st == RunProgress.Status.Stopping) return

        _uiState.update { it.copy(activeScenario = scenario, runResult = null, errorMessage = null, showStopConfirm = false) }

        executionJob = viewModelScope.launch {
            try {
                val runBundle = scenarioExecutor.execute(scenario)

                _uiState.update { it.copy(runResult = runBundle.result, pastResults = it.pastResults + runBundle.result) }

                val files = sessionExporter.exportRun(
                    runSession = runBundle.session,
                    runResult = runBundle.result,
                    events = runBundle.events
                )

                _uiEffect.tryEmit(LabUiEffect.ShowExportDialog(files))
                _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Scenario completed successfully"))
            } catch (ce: CancellationException) {
                // ✅ Cancelled را Error حساب نکن
                _uiState.update { it.copy(errorMessage = null) }
                _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Cancelled"))
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Execution failed") }
                _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Error: ${e.message}"))
            } finally {
                executionJob = null
                _uiState.update { it.copy(showStopConfirm = false) }
            }
        }
    }

    private fun requestStop() {
        val st = _uiState.value.progress.status
        if (st != RunProgress.Status.Running) {
            _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Nothing is running"))
            return
        }
        _uiState.update { it.copy(showStopConfirm = true) }
    }

    private fun confirmStop() {
        val job = executionJob
        _uiState.update { it.copy(showStopConfirm = false) }

        if (job == null) {
            _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Nothing is running"))
            return
        }

        _uiState.update { it.copy(progress = it.progress.copy(status = RunProgress.Status.Stopping)) }
        _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Stopping…"))
        job.cancel()
    }

    private fun retryLast() {
        val st = _uiState.value.progress.status
        if (st == RunProgress.Status.Running || st == RunProgress.Status.Stopping) return

        val preset = _uiState.value.lastPreset
        if (preset == null) {
            _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("No previous scenario"))
            return
        }
        startScenario(preset)
    }

    private fun copyLastRunSummary() {
        val r = _uiState.value.runResult
        val s = _uiState.value.activeScenario

        if (r == null || s == null) {
            _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Nothing to copy"))
            return
        }

        val text = buildString {
            appendLine("=== ChatLab Run Summary ===")
            appendLine("Scenario: ${s.name}")
            appendLine("Pattern: ${s.pattern}")
            appendLine("Duration: ${s.durationSec}s | RPS: ${s.rps} | Payload: ${s.payloadBytes} bytes")
            appendLine()
            appendLine("Enqueued: ${r.enqueuedCount}")
            appendLine("Sent: ${r.sentCount}")
            appendLine("Received: ${r.receivedCount}")
            appendLine("Failed: ${r.failedCount}")
            appendLine("SuccessRate: " + "%.2f".format(r.successRatePercent) + "%")
            appendLine("Throughput: " + "%.2f".format(r.throughputMsgPerSec) + " msg/s")
            appendLine("Latency avg/p50/p95/p99: " +
                "${r.latencyAvgMs ?: "-"} / ${r.latencyP50Ms ?: "-"} / ${r.latencyP95Ms ?: "-"} / ${r.latencyP99Ms ?: "-"} ms")
        }

        _uiEffect.tryEmit(LabUiEffect.CopyToClipboard(label = "Run summary", text = text))
    }
}