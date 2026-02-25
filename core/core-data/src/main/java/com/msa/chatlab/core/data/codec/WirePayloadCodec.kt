package com.msa.chatlab.core.data.codec

import com.msa.chatlab.core.domain.model.CodecMode
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.codec.StandardEnvelopeCodec
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload

class WirePayloadCodec {
    fun encode(profile: Profile, payload: OutgoingPayload): OutgoingPayload {
        return when (profile.payloadProfile.codec) {
            CodecMode.PlainText -> payload
            CodecMode.StandardEnvelope -> {
                val original = payload.envelope
                val wireText = StandardEnvelopeCodec.encode(original)

                val wireEnvelope = Envelope(
                    messageId = original.messageId,
                    createdAt = original.createdAt,
                    contentType = "text/plain",
                    headers = emptyMap(),
                    body = wireText.encodeToByteArray()
                )
                payload.copy(envelope = wireEnvelope)
            }
        }
    }
}
