package com.msa.chatlab.core.data.codec

import com.msa.chatlab.core.domain.model.ChaosAction
import com.msa.chatlab.core.domain.model.ChaosPolicy
import com.msa.chatlab.core.domain.model.DeliveryPolicy
import com.msa.chatlab.core.domain.model.MqttConfig
import com.msa.chatlab.core.domain.model.OutboxPolicy
import com.msa.chatlab.core.domain.model.PayloadPolicy
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.model.RetryPolicy
import com.msa.chatlab.core.domain.model.SignalRConfig
import com.msa.chatlab.core.domain.model.SocketIoConfig
import com.msa.chatlab.core.domain.model.TransportConfig
import com.msa.chatlab.core.domain.model.WsKtorConfig
import com.msa.chatlab.core.domain.model.WsOkHttpConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProfileJsonCodec {

    private val json = Json { prettyPrint = true }

    fun toCompactJson(profile: Profile): String {
        return Json.encodeToString(profile)
    }

    fun toPrettyJson(profile: Profile): String {
        return json.encodeToString(profile)
    }

    fun fromJson(jsonString: String): Profile {
        return Json.decodeFromString(jsonString)
    }

    internal fun encode(p: Profile): String = Json.encodeToString(p)

    internal fun decode(jsonString: String): Profile = Json.decodeFromString(jsonString)
}
