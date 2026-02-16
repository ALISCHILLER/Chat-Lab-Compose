package com.msa.chatlab.feature.lab.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.lab.*
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.feature.lab.state.LabUiEffect
import com.msa.chatlab.feature.lab.state.LabUiEvent
import com.msa.chatlab.feature.lab.state.LabUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LabViewModel(
    private val profileManager: ProfileManager,
    private val scenarioExecutor: ScenarioExecutor,
    private val sessionExporter: SessionExporter,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LabUiEffect>(extraBufferCapacity = 16)
    val uiEffect = _uiEffect.asSharedFlow()

    private var executionJob: Job? = null

    fun onEvent(event: LabUiEvent) {
        when (event) {
            is LabUiEvent.StartStable -> startScenario(Scenario.Preset.Stable)
            is LabUiEvent.StartIntermittent -> startScenario(Scenario.Preset.Intermittent)
            is LabUiEvent.StartOfflineBurst -> startScenario(Scenario.Preset.OfflineBurst)
            is LabUiEvent.StartLossy -> startScenario(Scenario.Preset.Lossy)
            is LabUiEvent.StartLoadBurst -> startScenario(Scenario.Preset.LoadBurst)
            is LabUiEvent.Stop -> stopExecution()
            is LabUiEvent.ClearResults -> _uiState.update { it.copy(runResult = null, errorMessage = null, pastResults = emptyList()) }
        }
    }

    private fun startScenario(preset: Scenario.Preset) {
        if (_uiState.value.isRunning) return

        val scenario = com.msa.chatlab.core.domain.model.lab.Scenario().defaultFor(preset)
        _uiState.update { it.copy(
            isRunning = true,
            activeScenario = scenario,
            runResult = null,
            progressPercent = 0,
            errorMessage = null
        ) }

        executionJob = viewModelScope.launch {
            try {
                // اجرای سناریو
                val runBundle = scenarioExecutor.execute(scenario)

                // به‌روزرسانی نتایج
                _uiState.update { it.copy(
                    isRunning = false,
                    runResult = runBundle.result,
                    progressPercent = 100,
                    pastResults = it.pastResults + runBundle.result
                ) }

                // صدور خروجی
                val files = sessionExporter.exportRun(
                    runSession = runBundle.session,
                    runResult = runBundle.result,
                    events = runBundle.events
                )

                _uiEffect.tryEmit(LabUiEffect.ShowExportDialog(files))
                _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Scenario completed successfully"))
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isRunning = false,
                    errorMessage = e.message ?: "Execution failed"
                ) }
                _uiEffect.tryEmit(LabUiEffect.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    private fun stopExecution() {
        executionJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }
}
