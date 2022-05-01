package utils

import data.User
import db.Users
import db.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

object UserDao {

    fun findByName(name: String): User = dbQuery {
        Users.select { Users.name eq name }.mapNotNull { toUser(it) }.first()
    }

    fun checkUser(user: User?): Boolean =
        user != null && dbQuery { Users.select { Users.name eq user.name }.mapNotNull { toUser(it) }.isNotEmpty() }

    private fun toUser(row: ResultRow): User =
        User(
            name = row[Users.name],
            password =  row[Users.password],
            admin = row[Users.admin]
        )
}