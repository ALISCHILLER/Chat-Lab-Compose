package com.msa.chatlab.protocol.mqtt.di

import com.msa.chatlab.core.data.registry.ProtocolBinding
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.protocol.mqtt.transport.MqttTransport
import org.koin.dsl.module

val MqttProtocolModule = module {
    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.MQTT
            override fun create(profile: Profile): TransportContract = MqttTransport(profile)
        }
    }
}
