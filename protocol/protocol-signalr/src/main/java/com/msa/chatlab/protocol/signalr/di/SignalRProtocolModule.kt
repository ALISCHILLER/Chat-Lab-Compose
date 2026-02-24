package com.msa.chatlab.protocol.signalr.di

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.TransportConfig
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.signalr.SignalRTransport
import org.koin.core.qualifier.named
import org.koin.dsl.module

val signalRProtocolModule = module {
    // This provides a factory for SignalRTransport.
    // The ConnectionManager will use this to create a new transport instance
    // whenever the active profile changes.
    factory<TransportContract>(named("signalr")) { params ->
        // The factory expects a TransportConfig to be passed in when created.
        val config: TransportConfig = params.get()
        SignalRTransport(config = config)
    }
}
