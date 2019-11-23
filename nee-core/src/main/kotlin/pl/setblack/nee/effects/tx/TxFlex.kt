package pl.setblack.nee.effects.tx

import io.vavr.collection.List
import io.vavr.control.Option
import pl.setblack.nee.Effect
import pl.setblack.nee.effects.Out
import pl.setblack.nee.effects.env.FlexibleEnv
import pl.setblack.nee.effects.env.ResourceId
import pl.setblack.nee.effects.security.*
import pl.setblack.nee.effects.tx.FlexTxProvider.Companion.txProviderResource
import java.lang.IllegalStateException

class FlexTxEffect<R>() : Effect<FlexibleEnv, TxError> {
    private val internal = TxEffect<R, FlexTxProvider<R>>()

    override fun <A, P> wrap(f: (FlexibleEnv) -> (P) -> A): (FlexibleEnv) -> Pair<(P) -> Out<TxError, A>, FlexibleEnv> =
        { env: FlexibleEnv ->
            val providerChance = env.get(txProviderResource)
                as Option<TxProvider<R,*>>
            providerChance.map { txProvider  ->
                val flexProvider = FlexTxProvider<R>(env)
                val internalF = { providerInternal: TxProvider<R, *> ->
                    f(env)
                }
                val wrapped = internal.wrap(internalF)
                val result = wrapped(flexProvider)
                Pair(result.first, env.set(result.second, txProviderResource))
            }.getOrElse(Pair({ _: P -> Out.left<TxError, A>(TxErrorType.NoConnection) }, env))
        }
}


class FlexTxProvider<R>(private val env: FlexibleEnv) :
    FlexibleEnv by env,
    TxProvider<R, FlexTxProvider<R>> {
    override fun getConnection(): TxConnection<R>  =
        env.get(txProviderResource).map { it.getConnection()  as TxConnection<R>}.getOrElseThrow{
            IllegalStateException("no connection for tx")
        }

    override fun setConnectionState(newState: TxConnection<R>): FlexTxProvider<R>
     =   FlexTxProvider(env.set(newState as TxProvider<*, *>, txProviderResource))


    companion object {
        val txProviderResource = ResourceId(TxProvider::class)
    }

}