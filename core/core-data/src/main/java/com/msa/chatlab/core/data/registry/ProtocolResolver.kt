package com.msa.chatlab.core.data.registry

import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.registry.ProtocolBinding
import javax.inject.Inject
import javax.inject.Provider

class ProtocolResolver @Inject constructor(
    private val bindings: Map<ProtocolType, @JvmSuppressWildcards Provider<ProtocolBinding>>
) {
    fun all(): List<ProtocolType> {
        return bindings.keys.toList()
    }
}
