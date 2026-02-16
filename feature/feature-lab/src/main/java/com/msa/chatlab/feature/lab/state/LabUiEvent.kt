package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.data.lab.Scenario

sealed interface LabUiEvent {
    data object StartStable : LabUiEvent
    data object StartIntermittent : LabUiEvent
    data object StartOfflineBurst : LabUiEvent
    data object StartLossy : LabUiEvent
    data object StartLoadBurst : LabUiEvent
    data object Stop : LabUiEvent
    data object ClearResults : LabUiEvent
}
