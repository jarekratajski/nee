package pl.setblack.nee.effects.tx

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.vavr.control.Either
import pl.setblack.nee.NEE

class DBEffectTest : BehaviorSpec({
    Given("TxEffects") {
        val eff = TxEffect<DBLike, DBLikeProvider>()
        val simpleAction = NEE.pure(eff) { db: DBLikeProvider ->
            val resource = db.getConnection().getResource()
            val result = resource.query("SELECT * FROM all1")
            result.map {
                Integer.parseInt(it)
            }.toEither<TxError>(TxErrorType.CannotQuery("I do not know"))
        }
        When("runned on db") {
            val db = DBLike()
            db.appendAnswer("6")
            val provider = DBLikeProvider(db)
            val result = simpleAction.perform(provider)
            Then("correct res") {
                println(result)
                result shouldBe Either.right<TxError, Int>(6)
                println(db.getLog())
            }
        }
        And("nested second action") {
            val nestedAction = { orig : Int ->
                NEE.pure(eff) { db: DBLikeProvider ->
                    val resource = db.getConnection().getResource()
                    val result = resource.query("SELECT * FROM all2 LIMIT ${orig})")
                    result.map {
                        Integer.parseInt(it) + 1000 + orig
                    }
                        .toEither<TxError>(TxErrorType.CannotQuery("I do not know"))
                }
            }
            val monad = simpleAction.flatMap (nestedAction)
            When("db connected") {
                val db = DBLike()
                db.appendAnswer("6")
                db.appendAnswer("70")
                val provider = DBLikeProvider(db)
                val result = monad.perform(provider)
                Then("correct res") {
                    println(result)
                    println(db.getLog())
                    result shouldBe Either.right<TxError, Int>(1076)
                }
            }
        }
        And("nested second action in internal tx") {
            val effReqNew = TxEffect<DBLike, DBLikeProvider>(true)
            val nestedAction = { orig : Int ->
                NEE.pure(effReqNew) { db: DBLikeProvider ->
                    val resource = db.getConnection().getResource()
                    val result = resource.query("SELECT * FROM all2 LIMIT ${orig})")
                    result.map {
                        Integer.parseInt(it) + 1000 + orig
                    }
                        .toEither<TxError>(TxErrorType.CannotQuery("I do not know"))
                }
            }
            val monad = simpleAction.flatMap (nestedAction)
            When("db connected") {
                val db = DBLike()
                db.appendAnswer("24")
                db.appendAnswer("700")
                val provider = DBLikeProvider(db)
                val result = monad.perform(provider)
                Then("correct res") {
                    println(result)
                    println(db.getLog())
                    result shouldBe Either.right<TxError, Int>(1724)
                }
            }
        }
    }
})