package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.domain.lab.RunProgress
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.model.ScenarioPreset

data class LabUiState(
    val activeScenario: Scenario? = null,
    val runResult: RunResult? = null,
    val progress: RunProgress = RunProgress(),
    val errorMessage: String? = null,
    val pastResults: List<RunResult> = emptyList(),

    // ✅ فاز ۱.۶
    val showStopConfirm: Boolean = false,
    val lastPreset: ScenarioPreset? = null
)