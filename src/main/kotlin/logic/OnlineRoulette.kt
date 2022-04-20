package logic

import kotlin.random.Random
import data.*


class OnlineRoulette {
    private val tableBets: MutableMap<User, MutableList<Bet>> = mutableMapOf()
    private var tableResults: MutableMap<User, Int> = mutableMapOf()
    private var result: Int = -1

    fun addBet(user: User, bet: Bet): Boolean {
        return if (bet.bet > 0 && (bet.type == "odd" || bet.type == "even" || (bet.type == "number" && bet.number in 0..36))) {
            tableBets[user]?.add(bet) ?: tableBets.put(user, mutableListOf(bet))
            true
        } else
            false
    }

    fun getBets(): List<Bet> {
        return tableBets.values.flatten()
    }

    fun gamble() {
        tableResults.clear()
        result = Random.nextInt(37)
        val type = if (result % 2 == 0) "even" else "odd"
        tableResults.putAll(tableBets.mapValues {
            it.value.sumOf { bet ->
                if (bet.type == type)
                    bet.bet * 2
                else if (bet.type == "number" && bet.number == result)
                    bet.bet * 4
                else
                    -bet.bet
            }
        })
        tableBets.clear()
    }

    fun getResult(): GambleResult = GambleResult(result)

    fun getUserResult(user: User): UserResult = UserResult(result, tableResults[user] ?: 0)
}