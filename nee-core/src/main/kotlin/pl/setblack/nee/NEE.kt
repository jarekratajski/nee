/**
Copyright 2019 Jarek Ratajski

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package pl.setblack.nee

import pl.setblack.nee.effects.Out

/**
 * Nee on function without param.
 *
 * Does not actually mean function is constant,
 * means it does not declare any params for Nee framework.
 */
typealias UNee<R, E, A> = Nee<R, E, Unit, A>

/**
 * Nee returning Any error.
 *
 * It is bad but practical. Having common (Any) error type
 * allows for monad binding.
 * I consider it less ugly then Error inheriting from Exception / Throwable
 * //CONTROVERSIAL
 */
typealias ANee<R, P, A> = Nee<R, Any, P, A>

/**
 *  Very Generic  Nee monad.
 *
 *  Error is any and does not declare any business param. (it is Unit)
 */
typealias UANee<R, A> = Nee<R, Any, Unit, A>

/**
 * Nee monad.
 *
 *  Naive enterprise effects
 *  Wraps business  function inside some "effects".
 *
 *
 *  @param R environment to run function on
 *  @param P param of function (business)
 *  //CONTROVERSIAL P is only added to handle caching
 *   - causes lot of complexity, does not give much
 *  @param E error that can happen (because of effects)
 *  @param A expected result type
 */
sealed class Nee<R, E, P, out A>(internal val effect: Effect<R, E>) {
    /**
     * Call Nee with given environment.
     *
     * @return function ready to apply business param
     */
    abstract fun perform(env: R): (P) -> Out<E, A>

    abstract fun <B> map(f: (A) -> B): Nee<R, E, P, B>
    abstract fun <B> flatMap(f: (A) -> Nee<R, E, P, B>): Nee<R, E, P, B>

    abstract fun constP(): (P) -> Nee<R, E, Unit, A>


    @Suppress("UNCHECKED_CAST")
    fun anyError(): ANee<R, P, A> = this as ANee<R, P, A>

    companion object {
        fun <A> pure(a: A): Nee<Any, Nothing, Nothing, A> =
            FNEE<Any, Nothing, Nothing, A>(NoEffect<Any, Nothing>(), extend({ _: Any -> { _: Nothing -> a } }))

        fun <R, E, P, A> constR(effect: Effect<R, E>, func: (P) -> A): Nee<R, E, P, A> =
            FNEE(effect, constR(func))

        fun <R, E, A> constP(effect: Effect<R, E>, func: (R) -> A): Nee<R, E, Unit, A> =
            FNEE(effect, constP(func))

        fun <R, E, P, A> pure(effect: Effect<R, E>, func: (R) -> (P) -> A): Nee<R, E, P, A> =
            FNEE(effect, extend(func))

    }
}

internal fun <T, T1> T.map(f: (T) -> T1) = f(this)
//CONTROVERSIAL


internal class FNEE<R, E, P, A>(
    effect: Effect<R, E>,
    private val func: (R) -> (P) -> Out<E, A>
) : Nee<R, E, P, A>(effect) {

    //constructor(effect: Effect<R, E>, f : (R)->(P)->A) : this(effect, {r:R-> {p:P-> Either.right<E,A>(f(r)(p))}} )

    private fun action() = effect.wrap(func)
    override fun perform(env: R): (P) -> Out<E, A> = { p: P -> action()(env).first(p).flatMap { it } }//f(env)
    //fun wrap(eff: Effect<R, E>): BaseENIO<R, E, A> = BaseENIO(f, effs.plusElement(eff).k())
    override fun <B> map(f: (A) -> B): Nee<R, E, P, B> =
        FNEE(effect) { r -> { p: P -> func(r)(p).map(f) } }

    override fun <B> flatMap(f: (A) -> Nee<R, E, P, B>): Nee<R, E, P, B> {
        val f2 = { r: R ->
            { p: P ->
                val z = func(r)(p).map(f)
                z.flatMap { it.perform(r)(p) }
            }
        }
        return FNEE(effect, f2)
    }

    override fun constP(): (P) -> Nee<R, E, Unit, A> = { p: P ->
        val f2 = { r: R ->
            { _: Unit ->
                this.perform(r)(p)
            }
        }
        FNEE(effect, f2)
    }

}

