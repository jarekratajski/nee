package dev.neeffect.nee.ctx.web

import dev.neeffect.nee.ANee

/**
 * Generic app context.
 */
interface ApplicationContextProvider<CTX, LOCAL> {
    suspend fun serve(businessFunction: ANee<CTX, Any>, localParam: LOCAL)
}

/**
 * Web application context.
 */
//class WebApplicationContextProvider(private val jdbcConfig : JDBCConfig) : ApplicationContextProvider<WebContext, ApplicationCall> {
//    override suspend fun serve(businessFunction: UANee<WebContext, Any>, localParam: ApplicationCall) {
//        WebContext.create(jdbcConfig, localParam).serveMessage(businessFunction, Unit)
//    }
//}
