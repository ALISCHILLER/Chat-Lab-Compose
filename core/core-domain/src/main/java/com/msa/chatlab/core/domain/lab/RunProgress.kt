package com.msa.chatlab.core.domain.lab

data class RunProgress(
    val status: Status = Status.Idle,
    val percent: Int = 0,
    val elapsedMs: Long = 0,
    val sentCount: Long = 0,
    val successCount: Long = 0,
    val failCount: Long = 0,
    val lastError: String? = null
) {
    enum class Status { Idle, Running, Completed, Failed }
}
