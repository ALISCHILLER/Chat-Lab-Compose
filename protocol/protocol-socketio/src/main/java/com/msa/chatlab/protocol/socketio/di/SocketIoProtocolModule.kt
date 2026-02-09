package com.msa.chatlab.protocol.socketio.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.socketio.transport.SocketIoTransport
import org.koin.dsl.module

val SocketIoProtocolModule = module {
    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.SOCKETIO
            override fun create(profile: Profile): TransportContract = SocketIoTransport(profile)
        }
    }
}
