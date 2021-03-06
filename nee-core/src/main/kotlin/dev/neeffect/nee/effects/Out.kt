package dev.neeffect.nee.effects

import io.vavr.concurrent.Future
import io.vavr.control.Either
import dev.neeffect.nee.effects.utils.merge


/**
 * Outcome of business function.
 *
 * It is ~ Future<Either<E,A>> (in vavr style0
 * the reason for not using vavr here was:
 *  - making critical api less depending on vavr
 *  - some efficiency (when result is in fdact immediate (see InstantOut)
 *
 *
 */
sealed class Out<E, out A> {

    abstract fun <B> map(f: (A) -> B): Out<E, B>

    abstract fun <E1> mapLeft(f: (E) -> E1): Out<E1, A>

    abstract fun <B> flatMap(f: (A) -> Out<E, B>): Out<E, B>

    abstract fun onComplete(f: (Either<E, out A>) -> Unit)

    abstract fun toFuture(): Future<out Either<E, out A>>

    companion object {
        fun <E, A> left(e: E): Out<E, A> = InstantOut(Either.left<E, A>(e));
        fun <E, A> right(a: A): Out<E, A> = InstantOut(Either.right<E, A>(a));
    }

    internal class InstantOut<E, A>(internal val v: Either<E, A>) : Out<E, A>() {
        override fun toFuture(): Future<Either<E, out A>> = Future.successful(v)

        override fun onComplete(f: (Either<E, out A>) -> Unit) = f(v)

        override fun <B> map(f: (A) -> B): Out<E, B> = InstantOut(v.map(f))

        override fun <E1> mapLeft(f: (E) -> E1): Out<E1, A> = InstantOut(v.mapLeft(f))

        override fun <B> flatMap(f: (A) -> Out<E, B>): Out<E, B> =
            v.map { a: A ->
                when (val res = f(a)) {
                    is InstantOut -> InstantOut(res.v)
                    is FutureOut -> FutureOut(res.futureVal)
                }
            }.mapLeft { _: E ->
                @Suppress("UNCHECKED_CAST")
                this as Out<E, B>
            }.merge()
    }

    internal class FutureOut<E, A>(internal val futureVal: Future<Either<E, A>>) : Out<E, A>() {
        override fun toFuture(): Future<Either<E, A>> = futureVal

        override fun onComplete(f: (Either<E, out A>) -> Unit) = futureVal.onComplete { value ->
            f(value.get())
        }.let { Unit }

        override fun <B> map(f: (A) -> B): Out<E, B> = FutureOut(futureVal.map { it.map(f) })

        override fun <E1> mapLeft(f: (E) -> E1): Out<E1, A> = FutureOut(futureVal.map { it.mapLeft(f) })

        override fun <B> flatMap(f: (A) -> Out<E, B>): Out<E, B> =
            FutureOut(futureVal.flatMap { e: Either<E, A> ->
                e.map { a: A ->
                    val z: Future<Either<E, B>> = when (val res = f(a)) {
                        is FutureOut -> res.futureVal
                        is InstantOut -> Future.successful(futureVal.executor(), res.v)
                    }
                    z
                }.mapLeft { e1 -> Future.successful(futureVal.executor(), Either.left<E, B>(e1)) }
                    .merge()
            })
    }
}
