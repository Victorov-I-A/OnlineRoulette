import com.google.gson.Gson
import data.Bet
import data.User
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.eclipse.jetty.websocket.api.Session
import utils.Authorization
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


@WebSocket
class WebSocketHandler {

    companion object {
        val userSessions: ConcurrentMap<Session, User> = ConcurrentHashMap()
    }

    @OnWebSocketConnect
    fun connected(session: Session) {
        Authorization.checkToken(
            session.upgradeRequest.headers["Authorization"]?.first().toString().substring(7)
        ).let { user ->
            if (user == null)
                session.close(1008, null)
            else {
                if (!userSessions.containsValue(user)) {
                    userSessions[session] = user
                    broadcastToOther("User ${user.name} join the gamble", session)
                }
                else
                    session.close(1008, null)
            }
        }
    }

    @OnWebSocketClose
    fun closed(session: Session, code: Int, reason: String?) {
        if (code == 1000) {
            broadcastToOther("User ${userSessions[session]!!.name} left the gamble", session)
        }
        userSessions.remove(session)
    }

    @OnWebSocketMessage
    fun message(session: Session, message: String) {
        val user = userSessions[session]!!
        when (Gson().fromJson(message, Request::class.java).request) {
            "logout" -> {
                session.close(1000, "User left the gamble")
            }
            "info" -> {
                send(
                    session,
                    Gson().toJson(onlineRoulette.getBets())
                )
            }
            "bet" -> {
                val bet = Gson().fromJson(message, Bet::class.java)
                if (onlineRoulette.addBet(user, bet))
                    send(
                        session,
                        Gson().toJson(Message("Bet accepted"))
                    )
                else
                    send(
                        session,
                        Gson().toJson(Message("Your request is not correct"))
                    )
            }
            "gamble" -> {
                if (user.admin) {
                    onlineRoulette.gamble()
                    broadcast(Gson().toJson(onlineRoulette.getResult()))
                } else
                    send(
                        session,
                        Gson().toJson(Message("You do not have permission to this action"))
                    )
            }
            "result" -> {
                send(
                    session,
                    Gson().toJson(onlineRoulette.getUserResult(user)
                    )
                )
            }
            else -> {
                send(
                    session,
                    Gson().toJson(Message("Your request is not correct"))
                )
            }
        }
    }

    private fun send(session: Session, message: String) = session.remote.sendString(message)

    private fun broadcast(message: String) {
        userSessions.keys.forEach { session ->
            try {
                send(session, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun broadcastToOther(message: String, sender: Session) {
        userSessions.keys.filter { it != sender }.forEach { session ->
            try {
                send(session, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class Message(val message: String)

    data class Request(val request: String)
}