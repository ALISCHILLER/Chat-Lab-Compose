package com.msa.chatlab.core.data.registry

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract

/**
 * هر protocol-xxx یک Binding می‌دهد:
 * - می‌گوید متعلق به چه ProtocolType است
 * - با Profile یک TransportContract می‌سازد
 */
interface ProtocolBinding {
    val type: ProtocolType
    fun create(profile: Profile): TransportContract
}
