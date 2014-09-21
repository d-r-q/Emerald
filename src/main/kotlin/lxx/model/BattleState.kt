package lxx.model

import robocode.Bullet
import java.util.HashMap

data class BattleState(val rules: BattleRules,
                       val time: Long,
                       val me: LxxRobot,
                       val enemy: LxxRobot,
                       val detectedBullets: Map<String, List<Bullet>>,
                       val prevState: BattleState? = null) {

    val battleField = rules.battleField

    private val maes: HashMap<Pair<LxxRobot, Int>, MaxEscapeAngle> = hashMapOf()

    fun robotByName(name: String) =
            if (name.equals(me.name)) me
            else enemy

    fun opponentOf(robot: LxxRobot) = when {
        robot identityEquals me -> enemy
        robot identityEquals enemy -> me
        else -> throw IllegalArgumentException("Unknown robot $robot")
    }

    fun preciseMaxEscapeAngle(victim: LxxRobot, bulletSpeed: Double): MaxEscapeAngle {
        assert(victim identityEquals me || victim identityEquals enemy, "Unknown robot $victim")
        return maes.getOrPut(Pair(victim, (bulletSpeed * 10).toInt()), { preciseMaxEscapeAngle(opponentOf(victim), victim, bulletSpeed) })
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is BattleState -> time == other.time
            else -> false
        }
    }

    override fun hashCode(): Int {
        return time.toInt()
    }

    override fun toString(): String {
        return "BattleState($time)"
    }
}