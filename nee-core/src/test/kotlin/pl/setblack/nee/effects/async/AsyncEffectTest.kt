package pl.setblack.nee.effects.async


import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.vavr.collection.List
import io.vavr.concurrent.Future
import io.vavr.concurrent.Promise
import io.vavr.control.Option
import pl.setblack.nee.Nee
import pl.setblack.nee.ignoreR
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AsyncEffectTest : DescribeSpec({
    describe("async context") {
        val controllableExecutionContext =  ControllableExecutionContext()
        val ecProvider = ECProvider(controllableExecutionContext)
        val eff = AsyncEffect<ECProvider>()
        context( "test function") {
            val runned = AtomicBoolean(false)
            val testFunction = { _:Unit -> runned.set(true)}
            val async =  Nee.Companion.pure(eff, ignoreR(testFunction))
            val performed = async.perform(ecProvider)(Unit)
            it("does not run before executor calls") {
                runned.get() shouldBe false
            }
            it("runs after async trigerred") {
                controllableExecutionContext.runSingle()
                runned.get() shouldBe true
            }
        }
        context ("with local ec") {
            val localEC = ControllableExecutionContext()
            //val localProvider = ECProvider(controllableExecutionContext)
            val localEff = AsyncEffect<ECProvider>(Option.some(localEC))
            val runned = AtomicBoolean(false)
            val testFunction = { _:Unit -> runned.set(true)}
            val async =  Nee.Companion.pure(localEff, ignoreR(testFunction))
            it( "will not run  on global") {
                val performed = async.perform(ecProvider)(Unit)
                controllableExecutionContext.runSingle()
                runned.get() shouldBe false
            }
            it ("will run on local ec") {
                localEC.runSingle()
                runned.get() shouldBe true
            }
        }

    }
})

class ControllableExecutionContext : ExecutionContext, Executor{
    override fun <T> execute(f: () -> T): Future<T> = executef(f)

    override fun execute(command: Runnable) :Unit  = executef( {  command.run()}).let{Unit}

    private val computations = AtomicReference(Computations())

    private fun <T> executef(f: () -> T): Future<T> =
        Promise.make<T>(InPlaceExecutor).let { promise ->
            val computation: Runnable = Runnable {
                promise.success(f())
            }
            computations.updateAndGet { it.addOne(computation) }
            promise.future()
        }

    internal fun runSingle() = computations.updateAndGet { list ->
        list.removeOne()
    }.lastOne?.run()
}

data class Computations(val computations : List<Runnable> = List.empty(), val lastOne : Runnable? = null ) {
        fun addOne( f: Runnable) = copy(computations = computations.append(f),
            lastOne = null)

        fun removeOne() = computations.headOption().map {runnable ->
            copy(computations = this.computations.pop(), lastOne =  runnable)
        }.getOrElse(Computations())
}