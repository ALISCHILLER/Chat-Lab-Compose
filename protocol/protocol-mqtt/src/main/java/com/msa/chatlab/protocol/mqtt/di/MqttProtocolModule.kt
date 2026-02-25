package com.msa.chatlab.protocol.mqtt.di

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.registry.ProtocolBinding
import com.msa.chatlab.protocol.mqtt.MqttTransport
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val MqttProtocolModule = module {
    factory { (profile: Profile) -> MqttTransport(profile) }

    factory<ProtocolBinding> {
        object : ProtocolBinding {
            override val type: ProtocolType = ProtocolType.MQTT
            override fun create(profile: Profile): TransportContract =
                get<MqttTransport> { parametersOf(profile) }
        }
    }
}
