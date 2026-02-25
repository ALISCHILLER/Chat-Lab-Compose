package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.ProfileId
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: ProfileId,
    val name: String,
    val description: String = "",
    val tags: List<String> = emptyList(),

    val protocolType: ProtocolType,
    val transportConfig: TransportConfig,

    val outboxPolicy: OutboxPolicy = OutboxPolicy(),
    val retryPolicy: RetryPolicy = RetryPolicy(),

    val deliveryPolicy: DeliveryPolicy = DeliveryPolicy(),

    val payloadPolicy: PayloadPolicy = PayloadPolicy(),
    val chaosPolicy: ChaosPolicy = ChaosPolicy()
)
