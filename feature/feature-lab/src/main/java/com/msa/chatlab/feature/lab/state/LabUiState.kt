package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.data.lab.Scenario
import com.msa.chatlab.core.data.lab.RunResult

data class LabUiState(
    val isRunning: Boolean = false,
    val activeScenario: Scenario? = null,
    val runResult: RunResult? = null,
    val progressPercent: Int = 0,
    val counters: Counters = Counters(),
    val errorMessage: String? = null,
    val pastResults: List<RunResult> = emptyList()
) {
    data class Counters(
        val sent: Long = 0,
        val received: Long = 0,
        val failed: Long = 0,
        val retried: Long = 0
    )
}
