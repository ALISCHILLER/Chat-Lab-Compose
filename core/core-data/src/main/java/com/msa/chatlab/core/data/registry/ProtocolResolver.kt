package com.msa.chatlab.core.data.registry

import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.registry.ProtocolBinding


class ProtocolResolver(
    bindings: List<ProtocolBinding>
) {
    private val supported = bindings.map { it.type }

    fun all(): List<ProtocolType> = supported
}
