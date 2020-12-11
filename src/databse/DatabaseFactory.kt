package id.ade.databse

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import id.ade.repository.Todos
import id.ade.repository.Users
import id.ade.util.Constant.Environment.DataBase.DB_NAME
import id.ade.util.Constant.Environment.DataBase.DB_PASSWORD
import id.ade.util.Constant.Environment.DataBase.JDBC_DATABASE_URL
import id.ade.util.Constant.Environment.DataBase.JDBC_DRIVER
import id.ade.util.Constant.Environment.DataBase.TRANSACTION_REPEATABLE_READ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(hikari())
        // Database is from the Exposed library.
        // It allows you to connect to the database with HikariDataSource, which your hikari method creates.

        // You use a transaction to create your Users and Todos tables.
        // It will only create the tables if they don’t already exist.
        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Todos)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        // import environment variables
        config.driverClassName = System.getenv(JDBC_DRIVER)
        config.jdbcUrl = System.getenv(JDBC_DATABASE_URL)
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = TRANSACTION_REPEATABLE_READ
        val user = System.getenv(DB_NAME)
        if (user != null) {
            config.username = user
        }
        val password = System.getenv(DB_PASSWORD)
        if (password != null) {
            config.password = password
        }
        config.validate()
        return HikariDataSource(config)
    }

    // 5
    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}


