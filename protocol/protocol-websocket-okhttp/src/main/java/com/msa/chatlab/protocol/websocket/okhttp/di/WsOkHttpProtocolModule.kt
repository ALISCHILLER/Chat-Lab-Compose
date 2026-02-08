package com.msa.chatlab.protocol.websocket.okhttp.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.websocket.okhttp.impl.WsOkHttpTransport
import org.koin.core.module.Module
import org.koin.dsl.module

val WsOkHttpProtocolModule = module {
    single<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.WS_OKHTTP
            override fun create(profile: Profile): TransportContract {
                return WsOkHttpTransport(profile = profile)
            }
        }
    }

    single {
        get<ProtocolRegistry>().apply {
            register(ProtocolType.WS_OKHTTP) { profile ->
                WsOkHttpTransport(profile = profile)
            }
        }
    }
}
