package com.msa.chatlab.feature.lab.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.lab.Scenario
import com.msa.chatlab.core.data.lab.ScenarioExecutor
import com.msa.chatlab.core.data.lab.SessionExporter
import com.msa.chatlab.core.data.lab.defaultFor
import com.msa.chatlab.core.domain.model.ScenarioPreset
import com.msa.chatlab.feature.lab.mapper.toDataPreset
import com.msa.chatlab.feature.lab.state.LabUiEffect
import com.msa.chatlab.feature.lab.state.LabUiEvent
import com.msa.chatlab.feature.lab.state.LabUiState
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
        // Observe progress from the executor
        viewModelScope.launch {
            scenarioExecutor.progress.collectLatest {
                _uiState.update { state -> state.copy(progress = it) }
            }
        }
    }

    fun onEvent(event: LabUiEvent) {
        when (event) {
            is LabUiEvent.StartStable -> startScenario(ScenarioPreset.Stable)
            is LabUiEvent.StartIntermittent -> startScenario(ScenarioPreset.Intermittent)
            is LabUiEvent.StartOfflineBurst -> startScenario(ScenarioPreset.OfflineBurst)
            is LabUiEvent.StartLossy -> startScenario(ScenarioPreset.Lossy)
            is LabUiEvent.StartLoadBurst -> startScenario(ScenarioPreset.LoadBurst)
            is LabUiEvent.Stop -> stopExecution()
            is LabUiEvent.ClearResults -> _uiState.update { it.copy(runResult = null, errorMessage = null, pastResults = emptyList()) }
        }
    }

    private fun startScenario(preset: ScenarioPreset) {
        if (_uiState.value.progress.status == com.msa.chatlab.core.domain.lab.RunProgress.Status.Running) return

        val scenario = Scenario().defaultFor(preset.toDataPreset())
        _uiState.update { it.copy(activeScenario = scenario, runResult = null, errorMessage = null) }

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
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Execution failed") }
                _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    private fun stopExecution() {
        executionJob?.cancel()
    }
}
