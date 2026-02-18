package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.data.lab.RunResult
import com.msa.chatlab.core.data.lab.Scenario
import com.msa.chatlab.core.domain.lab.RunProgress

data class LabUiState(
    val activeScenario: Scenario? = null,
    val runResult: RunResult? = null,
    val progress: RunProgress = RunProgress(),
    val errorMessage: String? = null,
    val pastResults: List<RunResult> = emptyList()
)
