package com.msa.chatlab.protocol.ws.ktor.di

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.registry.ProtocolBinding
import com.msa.chatlab.protocol.ws.ktor.KtorTransport
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val KtorWsProtocolModule = module {
    factory { (profile: Profile) -> KtorTransport(profile) }

    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.WS_KTOR
            override fun create(profile: Profile): TransportContract =
                get<KtorTransport> { parametersOf(profile) }
        }
    }
}
