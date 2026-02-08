package com.msa.chatlab.core.data.registry

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.contract.TransportContract

class ProtocolResolver(
    private val activeProfileStore: ActiveProfileStore,
    private val registry: ProtocolRegistry
) {
    fun currentProfileOrThrow(): Profile =
        activeProfileStore.activeProfile.value ?: error("No active profile selected")

    fun resolveCurrentTransport(): TransportContract {
        val profile = currentProfileOrThrow()
        return registry.create(profile)
    }
}
