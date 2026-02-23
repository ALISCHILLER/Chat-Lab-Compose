package com.msa.chatlab.protocol.signalr.di

import com.msa.chatlab.core.common.concurrency.AppScope
import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.signalr.SignalRTransport
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val SignalRProtocolModule = module {
    // This transport will get its own scope and can reuse the base OkHttpClient.
    factory { (profile: Profile) -> SignalRTransport(profile, get<AppScope>()) }

    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.SIGNALR
            override fun create(profile: Profile): TransportContract =
                get<SignalRTransport> { parametersOf(profile) }
        }
    }
}
