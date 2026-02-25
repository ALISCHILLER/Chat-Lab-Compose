package com.msa.chatlab.core.domain.value

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MessageIdSerializer::class)
@JvmInline
value class MessageId(val value: String)

@Serializable(with = ProfileIdSerializer::class)
@JvmInline
value class ProfileId(val value: String)

@Serializable(with = RunIdSerializer::class)
@JvmInline
value class RunId(val value: String)

object MessageIdSerializer : KSerializer<MessageId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MessageId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: MessageId) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): MessageId = MessageId(decoder.decodeString())
}

object ProfileIdSerializer : KSerializer<ProfileId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ProfileId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ProfileId) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): ProfileId = ProfileId(decoder.decodeString())
}

object RunIdSerializer : KSerializer<RunId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RunId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: RunId) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): RunId = RunId(decoder.decodeString())
}
