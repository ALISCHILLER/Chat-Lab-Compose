package com.msa.chatlab.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OutboxPolicy(
    val enabled: Boolean = false,
    val stallTimeoutMillis: Long = 2000,
)

@Serializable
data class RetryPolicy(
    val enabled: Boolean = false,
    val maxAttempts: Int = 3,
    val delayMillis: Long = 1000,
    val jitterRatio: Double = 0.2
)

@Serializable
data class DeliveryPolicy(
    val enabled: Boolean = false,
)

@Serializable
data class PayloadPolicy(
    val codec: CodecMode = CodecMode.StandardEnvelope,
    val percentage: Int? = null,
    val value: String? = null
)

@Serializable
enum class CodecMode {
    PlainText,
    StandardEnvelope
}
