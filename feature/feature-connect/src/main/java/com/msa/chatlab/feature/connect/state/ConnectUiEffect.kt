package com.msa.chatlab.feature.connect.state

import com.msa.chatlab.core.domain.error.AppError

sealed interface ConnectUiEffect {
    data class ShowError(val error: AppError) : ConnectUiEffect
}
