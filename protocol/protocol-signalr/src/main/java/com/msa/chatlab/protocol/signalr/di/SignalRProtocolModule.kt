package com.msa.chatlab.protocol.signalr.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.signalr.transport.SignalRTransport
import org.koin.dsl.module

val SignalRProtocolModule = module {
    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.SIGNALR
            override fun create(profile: Profile): TransportContract = SignalRTransport(profile)
        }
    }
}
