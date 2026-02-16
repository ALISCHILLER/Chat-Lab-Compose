package com.msa.chatlab.protocol.websocket.okhttp.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.websocket.okhttp.transport.WsOkHttpTransport
import org.koin.dsl.module

val WsOkHttpProtocolModule = module {
    factory { (profile: Profile) -> WsOkHttpTransport(profile) }
    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.WS_OKHTTP
            override fun create(profile: Profile): TransportContract = get<WsOkHttpTransport> { org.koin.core.parameter.parametersOf(profile) }
        }
    }
}
