package com.msa.chatlab.feature.lab.state

import com.msa.chatlab.core.domain.model.Scenario

sealed interface LabUiEvent {
    data class Start(val scenario: Scenario) : LabUiEvent

    // ✅ فاز ۱.۶: Stop با تایید
    data object StopPressed : LabUiEvent
    data object ConfirmStop : LabUiEvent
    data object DismissStopConfirm : LabUiEvent

    // برای سازگاری عقب‌رو (اگر جایی هنوز Stop را صدا می‌زند)
    data object Stop : LabUiEvent

    data object ClearResults : LabUiEvent

    data object StartStable : LabUiEvent
    data object StartIntermittent : LabUiEvent
    data object StartOfflineBurst : LabUiEvent
    data object StartLossy : LabUiEvent
    data object StartLoadBurst : LabUiEvent

    // ✅ فاز ۱.۶
    data object RetryLast : LabUiEvent
    data object CopyLastRunSummary : LabUiEvent
}