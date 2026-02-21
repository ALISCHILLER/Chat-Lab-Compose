package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.domain.lab.RunProgress
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.Scenario

data class LabUiState(
    val activeScenario: Scenario? = null,
    val runResult: RunResult? = null,
    val progress: RunProgress = RunProgress(),
    val errorMessage: String? = null,
    val pastResults: List<RunResult> = emptyList()
)
