package com.msa.chatlab.protocol.websocket.ktor.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.websocket.ktor.transport.KtorTransport
import org.koin.dsl.module

val KtorProtocolModule = module {
    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.WS_KTOR
            override fun create(profile: Profile): TransportContract = KtorTransport(profile)
        }
    }
}
