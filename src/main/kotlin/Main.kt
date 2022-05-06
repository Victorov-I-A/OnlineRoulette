import com.google.gson.Gson
import data.User
import db.DBFactory
import logic.OnlineRoulette
import spark.Spark.*
import utils.Authorization
import utils.UserDao


val onlineRoulette = OnlineRoulette()


fun main(args: Array<String>) {
    staticFileLocation("/public")

    secure("deploy/keystore.jks", "password", null, null);

    DBFactory.init()

    webSocket("/roulette", WebSocketHandler::class.java)
    init()

    post("/auth") { request, response ->
        val user = Gson().fromJson(request.body(), User::class.java)
        println(user)
        println(user == null)
        if (UserDao.checkUser(user)) {
            if (!WebSocketHandler.userSessions.containsValue(user)) {
                response.header("Authorization", "Bearer " + Authorization.generateToken(user))
                response.status(200)
            } else
                halt(403, "User is already logged in")
        } else
            halt(404, "Your credentials are not correct or user does not exist")
    }
}
