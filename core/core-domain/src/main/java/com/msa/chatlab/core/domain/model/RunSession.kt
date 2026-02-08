package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.TimestampMillis

data class RunSession(
    val appVersion: String,
    val deviceModel: String,
    val osVersion: String,
    val networkType: String,
    val startedAt: TimestampMillis
)
