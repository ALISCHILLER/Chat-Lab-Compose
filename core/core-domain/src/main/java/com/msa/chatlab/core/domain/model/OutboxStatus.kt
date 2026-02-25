package com.msa.chatlab.core.domain.model

enum class OutboxStatus {
    PENDING,
    IN_FLIGHT,
    SENT,
    FAILED,
}
