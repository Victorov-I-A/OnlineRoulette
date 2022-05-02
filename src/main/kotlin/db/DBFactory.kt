package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


object DBFactory {
    private object DBCredentials {
        const val USER = "postgres"
        const val PASSWORD = "postgres"
        const val URL = "jdbc:postgresql://roulette_db:5432/roulette"
    }

    fun init() {
        Database.connect(hikari())

        Flyway.configure()
            .dataSource(DBCredentials.URL, DBCredentials.USER, DBCredentials.PASSWORD)
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = DBCredentials.URL
        config.username = DBCredentials.USER
        config.password = DBCredentials.PASSWORD
        config.validate()
        return HikariDataSource(config)
    }
}

fun <T> dbQuery(block: () -> T): T = transaction { block() }
