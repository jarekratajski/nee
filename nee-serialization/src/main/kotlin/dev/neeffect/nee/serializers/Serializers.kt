package dev.neeffect.nee.serializers

import io.vavr.collection.List
import io.vavr.control.Option
import io.vavr.kotlin.toVavrList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

class UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString( value.toString())
}

object VavrSerializers {

    class ListSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<List<T>> {
        private val listSerializer = kotlinx.serialization.builtins.ListSerializer(dataSerializer)

        override fun deserialize(decoder: Decoder): List<T> = listSerializer.deserialize(decoder).toVavrList()

        override val descriptor: SerialDescriptor = listSerializer.descriptor

        override fun serialize(encoder: Encoder, value: List<T>) = listSerializer.serialize(
            encoder, value.toJavaList()
        )
    }

    @kotlinx.serialization.ExperimentalSerializationApi
    class OptionSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<Option<T>> {

        override fun deserialize(decoder: Decoder): Option<T> =
            if (decoder.decodeNotNullMark()) {
                Option.of(dataSerializer.deserialize(decoder))
            } else {
                Option.none()
            }

        override val descriptor: SerialDescriptor = SerialDescriptorForOption(dataSerializer.descriptor)

        override fun serialize(encoder: Encoder, value: Option<T>) =
            value.map { w ->
                encoder.encodeNotNullMark()
                dataSerializer.serialize(encoder, w)
            }.getOrElse {
                encoder.encodeNull()
            }

    }

    @kotlinx.serialization.ExperimentalSerializationApi
    internal class SerialDescriptorForOption(internal val original: SerialDescriptor) : SerialDescriptor by original {
        override val serialName: String = "Option<${original.serialName}>"
        override val isNullable: Boolean
            get() = true

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SerialDescriptorForOption) return false
            if (original != other.original) return false
            return true
        }

        override fun toString(): String {
            return "Option<$original>"
        }

        override fun hashCode(): Int {
            return original.hashCode() * 31
        }
    }
}
