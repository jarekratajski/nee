package dev.neeffect.nee.effects.cache


import dev.neeffect.nee.Effect
import dev.neeffect.nee.effects.Out
import java.util.concurrent.ConcurrentHashMap

class CacheEffect<R, E>(
    private val cacheProvider: CacheProvider
) : Effect<R, E> {
    override fun <A, P> wrap(f: (R) -> (P) -> A): (R) -> Pair<(P) -> Out<E, A>, R> = { r: R ->
        Pair({ p: P ->
            Out.right<E,A>(cacheProvider.computeIfAbsent(p, { f(r)(p) }))
        }, r)
    }
}

interface CacheProvider {
    fun <K, V> computeIfAbsent(key: K, func: (K) -> V): V
}

class NaiveCacheProvider : CacheProvider {
    private val map: ConcurrentHashMap<Any, Any> = ConcurrentHashMap()
    @Suppress("UNCHECKED_CAST")
    override fun <K, V> computeIfAbsent(key: K, func: (K) -> V): V =
        map.computeIfAbsent(key as Any) { k:Any -> func(k as K) as Any} as V
}
