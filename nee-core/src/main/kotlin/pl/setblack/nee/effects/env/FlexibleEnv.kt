package pl.setblack.nee.effects.env

import io.vavr.control.Option
import io.vavr.control.Option.some
import kotlin.reflect.KClass


data class ResourceId<T : Any>(val clazz: KClass<T>, val key: Any = DefaultKey) {
    object DefaultKey
}

//TODO actually needed sealed class but it did not work
interface  FlexibleEnv {
    abstract fun <T : Any> get(id: ResourceId<T>): Option<T>
    abstract fun <T : Any> set(t: T, id: ResourceId<T>): FlexibleEnv

    companion object {
        inline fun <reified T : Any> create(
            t: T,
            id: ResourceId<T> = ResourceId(T::class)
        ) =
            WrappedEnv(t, id, EnvLeaf)
    }
}

object EnvLeaf : FlexibleEnv {
    override fun <T : Any> get(id: ResourceId<T>): Option<T> = Option.none()

    override fun <T : Any> set(t: T, id: ResourceId<T>): FlexibleEnv =
        throw IllegalArgumentException("Impossible to set resource of type ${id}")
}

data class WrappedEnv<Y : Any>(
    private val env: Y,
    private val resId: ResourceId<Y>,
    private val inner: FlexibleEnv
) : FlexibleEnv {
    override fun <T : Any> get(id: ResourceId<T>): Option<T> =
        if (id == resId) {
            @Suppress("UNCHECKED_CAST")
            some(env) as Option<T>
        } else {
            inner.get(id)
        }

    override fun <T : Any> set(t: T, id: ResourceId<T>): FlexibleEnv =
        if (id == resId) {
            WrappedEnv(t, id, inner)
        } else {
            WrappedEnv(env, resId, inner.set(t, id))
        }

}