package db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val name: Column<String> = text("name")
    val password: Column<String> = text("password")
    val admin: Column<Boolean> = bool("admin").default(false)

    override val primaryKey = PrimaryKey(name)
}
