package pl.setblack.nee.effects.tx

import io.github.classgraph.Resource
import io.vavr.control.Either
import pl.setblack.nee.Effect


data class TestResource(val version: Int)


class TestEffect(val name: String, val log: MutableList<String>) : Effect<TestResource, String> {
    override fun <A, P> wrap(f: (TestResource) -> (P) -> A): (TestResource) -> Pair<(P) -> Either<String, A>, TestResource> = { r: TestResource ->
             log("enter test effect. Res $r")
             Pair(
             { p:P ->
                 val newR = r.copy(version = r.version +10)
                 log("calling test effect. Res $newR")
                 Either.right<String, A>(f(newR)(p))
             }, r.copy(version = r.version +100))
    }

    fun log(msg : String) = log.add("$name: $msg" )
}