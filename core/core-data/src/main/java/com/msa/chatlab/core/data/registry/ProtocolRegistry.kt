package com.msa.chatlab.core.data.registry

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract

class ProtocolRegistry(
    bindings: List<ProtocolBinding>
) {
    private val map: Map<ProtocolType, ProtocolBinding> = bindings.associateBy { it.type }

    fun has(type: ProtocolType): Boolean = map.containsKey(type)

    fun create(profile: Profile): TransportContract {
        val binding = map[profile.protocolType]
            ?: error("No ProtocolBinding registered for ${profile.protocolType}")
        return binding.create(profile)
    }
}
