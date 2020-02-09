package pl.setblack.nee.effects.security

import io.vavr.collection.List
import pl.setblack.nee.effects.Out

class SimpleSecurityProvider<USER, ROLE>(user: USER, roles: List<ROLE>) : SecurityProvider<USER, ROLE> {
    private val ctx = SimpleSecurityContext(user, roles)
    override fun getSecurityContext(): Out<SecurityError, SecurityCtx<USER, ROLE>> = Out.right(ctx)

    internal class SimpleSecurityContext<USER, ROLE>(private val user: USER, private val roles: List<ROLE>) :
        SecurityCtx<USER, ROLE> {
        override fun getCurrentUser(): Out<SecurityError, USER> = Out.right(user)
        override fun hasRole(role: ROLE): Boolean = roles.contains(role)
    }
}
