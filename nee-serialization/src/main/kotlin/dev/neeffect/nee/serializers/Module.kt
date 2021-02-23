package dev.neeffect.nee.serializers

import io.vavr.control.Option
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass


val module = SerializersModule {
    val anyOptionSerializer = serializer(Option::class.java) as KSerializer<Option<*>>
    val zSerializer=  Option::class.serializer()
    contextual(UUIDSerializer())
    polymorphic(Option::class) {
        default { _ ->
            anyOptionSerializer
        }
    }
}

//val vavrClasses = arrayOf(VListSerializer::class, OptionSerializer::class)

