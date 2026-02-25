package com.msa.chatlab.protocol.signalr.di


import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.model.SignalRConfig
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.registry.ProtocolBinding
import com.msa.chatlab.protocol.signalr.SignalRTransport
import org.koin.dsl.module

val signalRProtocolModule = module {
    factory { (profile: Profile) ->
        val cfg = profile.transportConfig
        require(cfg is SignalRConfig) { "Profile transportConfig is not SignalRConfig" }
        SignalRTransport(config = cfg)
    }

    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.SIGNALR
            override fun create(profile: Profile): TransportContract =
                get<SignalRTransport> { org.koin.core.parameter.parametersOf(profile) }
        }
    }
}
