import logic.OnlineRoulette
import utils.*
import data.*
import com.google.gson.*
import spark.Request
import spark.Spark.*


class Server {
    fun start(port: Int) {
        val onlineRoulette = OnlineRoulette()

        port(port)

        path("/auth") {
            post("/login") { request, response ->
                val user = Gson().fromJson(request.body(), User::class.java)
                if (UserDao.checkUser(user)) {
                    if (!authorization(request, user)) {
                        request.session().attribute(user.name, "Authorization")
                        response.header("Authorization", "Bearer " + Authorization.generateToken(user))
                        response.status(200)
                    } else
                        halt(403, "User is already logged in")
                } else
                    halt(404, "Your credentials are not correct or user does not exist")
            }

            delete("/logout") { request, response ->
                val user = authentication(request)
                if (authorization(request, user)) {
                    request.session().removeAttribute(user.name)
                    response.status(200)
                } else
                    halt(401, "User is not logged in")
            }
        }
        path("/game") {
            get("/info") { request, response ->
                val user = authentication(request)
                if (authorization(request, user)) {
                    response.status(200)
                    return@get Gson().toJson(onlineRoulette.getBets())
                } else
                    halt(401, "User is not logged in")
            }

            post("/bet") { request, response ->
                val user = authentication(request)
                if (authorization(request, user)) {
                    val bet = Gson().fromJson(request.body(), Bet::class.java)
                    if (onlineRoulette.addBet(user, bet))
                        response.status(200)
                    else
                        halt(400, "Your request is not correct")
                } else
                     halt(401, "User is not logged in")
            }

            put("/gamble") { request, response ->
                val user = authentication(request)
                if (authorization(request, user)) {
                    if (user.admin) {
                        onlineRoulette.gamble()
                        response.status(200)
                        return@put Gson().toJson(onlineRoulette.getResult())
                    } else
                        halt(403, "You do not have permission to this action")
                } else
                    halt(401, "User is not logged in")
            }

            get("/result") { request, response ->
                val user = authentication(request)
                if (authorization(request, user)) {
                    response.status(200)
                    return@get Gson().toJson(onlineRoulette.getUserResult(user))
                } else
                    halt(401, "User is not logged in")
            }
        }
    }

    private fun authentication(request: Request): User {
        val token = request.headers("Authorization").substring(7)
        Authorization.checkToken(token).let {
            if (it == null)
                throw halt(400, "Your token is missing or invalid")
            else
                return it
        }
    }

    private fun authorization(request: Request, user: User): Boolean = request.session().attributes().contains(user.name)
}
