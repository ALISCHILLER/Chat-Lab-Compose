package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.domain.model.Scenario

sealed interface LabUiEvent {
    data class Start(val scenario: Scenario) : LabUiEvent
    data object Stop : LabUiEvent
    data object ClearResults : LabUiEvent
    data object StartStable : LabUiEvent
    data object StartIntermittent : LabUiEvent
    data object StartOfflineBurst : LabUiEvent
    data object StartLossy : LabUiEvent
    data object StartLoadBurst : LabUiEvent
}
