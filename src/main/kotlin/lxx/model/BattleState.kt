package lxx.model

import robocode.Bullet

data class BattleState(val rules: BattleRules,
                       val time: Long,
                       val me: LxxRobot,
                       val enemy: LxxRobot,
                       val detectedBullets: Map<String, List<Bullet>>,
                       val prevState: BattleState? = null) {

    val battleField = rules.battleField

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is BattleState -> time == other.time
            else -> false
        }
    }

    override fun hashCode(): Int {
        return time.toInt()
    }

    fun robotByName(name: String) =
            if (name.equals(me.name)) me
            else enemy

}