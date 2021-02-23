@file:UseSerializers(VavrSerializers.OptionSerializer::class, VavrSerializers.ListSerializer::class)
package dev.neeffect.nee.ctx.web.util

import dev.neeffect.nee.ctx.web.ErrorHandler
import dev.neeffect.nee.effects.Out
import dev.neeffect.nee.effects.utils.Logging
import dev.neeffect.nee.effects.utils.logger
import dev.neeffect.nee.effects.utils.merge
import dev.neeffect.nee.serializers.VavrSerializers
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.response.respond
import io.vavr.control.Either
import kotlinx.coroutines.future.await
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class  RenderHelper(
    val jsonMapper: Json,
    private val errorHandler: ErrorHandler
) : Logging {

    suspend fun renderText(call: ApplicationCall, text:String) =
        TextContent(
            text = text,
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.OK
        ).let {
            call.respond(it)
        }

    suspend fun <T> renderResponse(call: ApplicationCall, resp: Either<ApiError, T>) =
        resp.mapLeft { error ->
            TextContent(
                text = error.toString(),
                contentType = ContentType.Text.Plain,
                status = error.status
            )
        }.map { result ->
            when (result) {
                is String ->
                    TextContent(
                        text = result,
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.OK
                    )
                else -> TODO()
            }
        }.merge().let { content ->
                 call.respond(content)
        }

    suspend inline fun <E, reified A> serveMessage(applicationCall:ApplicationCall, msg: Out<E, A>): Unit =
        msg.toFuture().toCompletableFuture().await().let { outcome ->
            val message = outcome.bimap<OutgoingContent, OutgoingContent>({ serveError(it as Any) }, { regularResult ->
                val bytes = jsonMapper.encodeToString(regularResult).toByteArray()
               // val bytes = objectMapper.writeValueAsBytes(regularResult)
                ByteArrayContent(
                    bytes = bytes,
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            }).merge()
            try {
                applicationCall.respond(message)
            } catch (e: Exception) {
                logger().warn("exception in sending response", e)
            }
        }

    suspend fun <E> serveText(applicationCall:ApplicationCall, msg: Out<E, String>): Unit =
        msg.toFuture().toCompletableFuture().await().let { outcome ->
            val message = outcome.bimap<OutgoingContent, OutgoingContent>({ serveError(it as Any) }, { regularResult ->
                TextContent(
                    text = regularResult,
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            }).merge()
            try {
                applicationCall.respond(message)
            } catch (e: Exception) {
                logger().warn("exception in sending response", e)
            }
        }


    fun serveError(errorResult: Any): OutgoingContent = errorHandler(errorResult)

}

sealed class ApiError {
    open val status: HttpStatusCode = HttpStatusCode.InternalServerError

    data class WrongArguments(val msg: String) : ApiError() {
        override val status = HttpStatusCode.BadRequest
    }
}
