package pl.setblack.nee.effects.security

import io.vavr.collection.List
import io.vavr.control.Either
import pl.setblack.nee.Effect

interface SecurityCtx<USER, ROLE> {
    fun getCurrentUser(): USER
    fun hasRole(role: ROLE): Boolean
}

interface SecurityProvider <USER,ROLE> {
    fun getSecurityContext() : Either<SecurityError, SecurityCtx<USER, ROLE>>
}

interface SecurityError {
    fun secError() : SecurityErrorType
}

sealed class SecurityErrorType : SecurityError{
    override fun secError() = this

    object UnknownUser : SecurityErrorType()
    object NoSecurityCtx : SecurityErrorType()
    data class MissingRole<ROLE>(val roles : List<ROLE>): SecurityErrorType()
}

class SecuredRunEffect<USER, ROLE, R : SecurityProvider<USER, ROLE>>(
    private val roles: List<ROLE>
) : Effect<R,
        SecurityError> {

    constructor(singleRole : ROLE) : this(List.of(singleRole))

    override fun <A> wrap(f: (R) -> Either<SecurityError, A>): (R) -> Pair<Either<SecurityError, A>, R> {
        return { provider:R ->
            Pair( provider.getSecurityContext() .flatMap { securityCtx ->
                val missingRoles = roles.filter {role ->
                    !securityCtx.hasRole(role)
                }
                if ( missingRoles.isEmpty) {
                    f(provider)
                } else {
                    Either.left<SecurityError, A>(SecurityErrorType.MissingRole(missingRoles))
                }
            }, provider)
        }
    }
}

