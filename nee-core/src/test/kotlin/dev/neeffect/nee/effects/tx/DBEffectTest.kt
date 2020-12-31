package dev.neeffect.nee.effects.tx

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.beInstanceOf
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import dev.neeffect.nee.Nee
import dev.neeffect.nee.effects.test.get
import dev.neeffect.nee.effects.test.getLeft

class DBEffectTest : BehaviorSpec({
    Given("TxEffects") {
        val eff = TxEffect<DBLike, DBLikeProvider>()
        val simpleAction = Nee.pure(eff, function1)

        When("run on db") {
            val db = DBLike()
            db.appendAnswer("6")
            val provider = DBLikeProvider(db)
            val result = simpleAction.perform(provider)
            Then("correct res") {
                result(Unit).get() shouldBe 6
                //println(db.getLog())
            }
        }
        And("nested second action") {
            val nestedAction = { orig: Int ->
                Nee.pure(eff, function2(orig))
            }
            val monad = simpleAction.flatMap(nestedAction)
            When("db connected") {
                val db = DBLike()
                db.appendAnswer("6")
                db.appendAnswer("70")
                val provider = DBLikeProvider(db)
                val result = monad.perform(provider)
                Then("correct res") {
                    result(Unit).get() shouldBe (1076)
                }
            }
        }
        And("nested second action in internal tx") {
            val effReqNew = TxEffect<DBLike, DBLikeProvider>(true)
            val nestedAction = { orig: Int ->
                Nee.pure(effReqNew, function2(orig))
            }
            val monad = simpleAction.flatMap(nestedAction)
            When("internal  action executed") {
                val db = DBLike()
                db.appendAnswer("24")
                db.appendAnswer("700")
                val provider = DBLikeProvider(db)
                val result = monad.perform(provider)
                Then("correct res") {
                    result(Unit).get()  shouldBe(1724)
                }
            }

        }
        And(" internal tx is detected") {
            val effReqNew = TxEffect<DBLike, DBLikeProvider>(true)
            val extractTxLevel = { _: Int ->
                Nee.pure(effReqNew) { r ->
                    {_: Unit ->
                        val connection= r.conn as DBConnection
                        connection.level
                    }
                }
            }
            val monad = simpleAction.flatMap(extractTxLevel)

            When("internal  action executed") {
                val db = DBLike()
                db.appendAnswer("24")
                val provider = DBLikeProvider(db)
                val result = monad.perform(provider)(Unit)
                Then("detected correct level of nested tx ") {
                    result.get()  shouldBe(2)
                }
            }
            //below section documents sometimes not exact execution of flatMap
            When("double internal  action executed") {
                val monad2 = simpleAction.flatMap{ extractTxLevel(it).flatMap(extractTxLevel)}
                val db = DBLike()
                db.appendAnswer("24")
                val provider = DBLikeProvider(db)
                val result = monad2.perform(provider)(Unit)
                Then("detected correct level of nested tx") {
                    result.get()  shouldBe(3)
                }
            }
            When("double internal  action executed - outside") {
                val monad2 = simpleAction.flatMap(extractTxLevel).flatMap(extractTxLevel)
                val db = DBLike()
                db.appendAnswer("24")
                val provider = DBLikeProvider(db)
                val result = monad2.perform(provider)(Unit)
                Then("detected same level of nested tx") {
                    result.get()  shouldBe(3)
                }
            }
        }
        When("running query with forced exception") {
            val failingAction = Nee.pure(eff, functionWithFailQuery)
            val db = DBLike()
            db.appendAnswer("6")
            val provider = DBLikeProvider(db)
            val result = failingAction.perform(provider)
            Then("expect error as result") {
                result(Unit).getLeft() should beInstanceOf ( TxErrorType::class)
            }
        }
    }
}) {
    companion object {
        internal val function1 = { db: DBLikeProvider ->
            { _: Unit ->
                val resource = db.getConnection().getResource()
                val result = resource.query("SELECT * FROM all1")
                result.map {
                    Integer.parseInt(it)
                }.get()
            }
        }

        internal val functionWithFailQuery = { db: DBLikeProvider ->
            { _: Unit ->
                val resource = db.getConnection().getResource()
                resource.raiseExceptionOnNext()
                val result = resource.query("SELECT * FROM all1")
                result.map {
                    Integer.parseInt(it)
                }.get()
            }
        }
        internal val function2 = { orig : Int ->
            { db: DBLikeProvider ->
                { _: Unit ->
                    val resource = db.getConnection().getResource()
                    val result = resource.query("SELECT * FROM all2 LIMIT ${orig})")
                    result.map {
                        Integer.parseInt(it) + 1000 + orig
                    }.get()
                }
            }
        }
    }
}
