package com.msa.chatlab.featurelab.state

import com.msa.chatlab.core.data.lab.SessionExporter

sealed interface LabUiEffect {
    data class ShowSnackbar(val message: String) : LabUiEffect
    data class ShowExportDialog(val bundle: SessionExporter.ExportBundle) : LabUiEffect
}
