package com.msa.chatlab.protocol.socketio.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.socketio.SocketIoTransport
import okhttp3.OkHttpClient
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val SocketIoProtocolModule = module {
    // This transport will reuse the base OkHttpClient defined in another module.
    factory { (profile: Profile) -> SocketIoTransport(profile, get<OkHttpClient>()) }

    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.SOCKETIO
            override fun create(profile: Profile): TransportContract =
                get<SocketIoTransport> { parametersOf(profile) }
        }
    }
}
