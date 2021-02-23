package dev.neeffect.nee.security

import dev.neeffect.nee.serializers.UUIDSerializer
import dev.neeffect.nee.serializers.VavrSerializers
import io.vavr.collection.List
import io.vavr.kotlin.toVavrList
import kotlinx.serialization.Serializable
import java.util.*


@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val login: String,
    @Serializable(with = VavrSerializers.ListSerializer::class)
    val roles: List<UserRole>,
    val displayName:String = ""
) {
    constructor(id: UUID, login: String, roles: List<UserRole>):this(id, login, roles, login)
}

@Serializable
data class UserRole(val roleName: String) {
    companion object {
        fun roles(vararg names: String): List<UserRole> = names.toVavrList()
            .map { UserRole(it) }
    }
}

