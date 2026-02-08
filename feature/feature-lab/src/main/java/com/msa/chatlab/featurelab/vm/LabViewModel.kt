package com.msa.chatlab.featurelab.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.lab.*
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.featurelab.state.LabUiEffect
import com.msa.chatlab.featurelab.state.LabUiEvent
import com.msa.chatlab.featurelab.state.LabUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

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
            is LabUiEvent.ClearResults -> _uiState.value = _uiState.value.copy(runResult = null, errorMessage = null)
        }
    }

    private fun startScenario(preset: Scenario.Preset) {
        if (_uiState.value.isRunning) return

        val scenario = scenario.defaultFor(preset)
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            activeScenario = scenario,
            runResult = null,
            progressPercent = 0,
            errorMessage = null
        )

        executionJob = viewModelScope.launch {
            try {
                // اجرای سناریو
                val result = scenarioExecutor.execute(scenario)

                // به‌روزرسانی نتایج
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    runResult = result,
                    progressPercent = 100
                )

                // صدور خروجی
                val bundle = sessionExporter.exportRun(
                    runSession = requireNotNull(scenarioExecutor.currentRun),
                    runResult = result,
                    events = scenarioExecutor.events,
                    outputDir = File(context.filesDir, "lab_runs/${System.currentTimeMillis()}")
                )

                _uiEffect.emit(LabUiEffect.ShowExportDialog(bundle))
                _uiEffect.emit(LabUiEffect.ShowSnackbar("Scenario completed successfully"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    errorMessage = e.message ?: "Execution failed"
                )
                _uiEffect.emit(LabUiEffect.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    private fun stopExecution() {
        executionJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }
}
