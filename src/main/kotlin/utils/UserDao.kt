package utils

import data.User

object UserDao {
    private val users = mutableListOf(
        User(name = "Ilia", password = "qwerty123", admin = true),
        User(name = "player1", password = "123", admin = false),
        User(name = "player2", password = "123", admin = false),
        User(name = "player3", password = "123", admin = false)
    )

    fun addUser(user: User) = users.add(user)

    fun removeUser(user: User) = users.remove(user)

    fun findByName(name: String): User? {
        return users.find { it.name == name }
    }

    fun checkUser(user: User?): Boolean = user != null && users.contains(user)
}