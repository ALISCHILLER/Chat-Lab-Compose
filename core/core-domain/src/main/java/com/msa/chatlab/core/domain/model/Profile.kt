package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.ProfileId

data class Profile(
    val id: ProfileId,
    val name: String,
    val description: String = "",
    val tags: List<String> = emptyList(),

    val protocolType: ProtocolType,
    val transportConfig: TransportConfig,

    val deliverySemantics: DeliverySemantics = DeliverySemantics.AtLeastOnce,
    val ackStrategy: AckStrategy = AckStrategy.TransportLevel,

    val outboxPolicy: OutboxPolicy = OutboxPolicy(),
    val retryPolicy: RetryPolicy = RetryPolicy(),
    val reconnectPolicy: ReconnectPolicy = ReconnectPolicy(),

    val payloadProfile: PayloadProfile = PayloadProfile(),
    val chaosProfile: ChaosProfile = ChaosProfile()
)
