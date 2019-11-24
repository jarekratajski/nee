package pl.setblack.nee.security

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import pl.setblack.nee.effects.cache.NaiveCacheProvider
import pl.setblack.nee.effects.jdbc.JDBCProvider
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

//object DBTestConnection {
//    val dbUrl = "jdbc:h2:mem:test_mem;DB_CLOSE_DELAY=-1"
//    val dbUser = "sa"
//    val dbPassword = ""
//    fun initializeDb() =
//        createDbConnection().use { dbConnection ->
//            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(dbConnection))
//            val liquibaseChangeLog = Liquibase("db/db.xml", ClassLoaderResourceAccessor(), database)
//            liquibaseChangeLog.update(liquibase.Contexts(), liquibase.LabelExpression())
//        }.let { createDbConnection() }
//
//    fun createDbConnection() = DriverManager.getConnection(
//        dbUrl,
//        dbUser,
//        dbPassword
//    )
//}