package pl.outside.code

import dev.neeffect.nee.Nee
import dev.neeffect.nee.NoEffect
import dev.neeffect.nee.effects.monitoring.CodeNameFinder.guessCodePlaceName
import dev.neeffect.nee.effects.monitoring.TraceProvider

object ExternalObject {
    fun plainFunction(i : Int) = i+1

    fun <R : TraceProvider<R>> traceableFunction(mon: R) =
        mon.getTrace().putNamedPlace(guessCodePlaceName(2)).let { _ -> ::plainFunction}

    fun checkWhereCodeIsSimple(a: Unit) = guessCodePlaceName()

    fun checkWhereCodeIsNee() = Nee.Companion.pure(NoEffect<Unit,Unit>()){ {_:Unit ->
        guessCodePlaceName()

    } }
}
