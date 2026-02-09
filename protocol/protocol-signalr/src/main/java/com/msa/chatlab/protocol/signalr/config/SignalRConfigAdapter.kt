package com.msa.chatlab.protocol.signalr.config

import com.microsoft.signalr.HttpHubConnectionBuilder
import com.microsoft.signalr.HubConnection
import com.msa.chatlab.core.domain.model.SignalRConfig

object SignalRConfigAdapter {
    fun SignalRConfig.toHubConnectionBuilder(): HttpHubConnectionBuilder {
        val builder = HttpHubConnectionBuilder.create(endpoint)
        headers.forEach { (key, value) ->
            builder.withHeader(key, value)
        }
        return builder
    }
}
