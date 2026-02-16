package com.msa.chatlab.feature.lab.state

sealed interface LabUiEffect {
    data class ShowExportDialog(val files: Map<String, String>) : LabUiEffect
    data class ShowSnackbar(val message: String) : LabUiEffect
}
