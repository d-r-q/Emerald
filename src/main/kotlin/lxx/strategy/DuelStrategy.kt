package lxx.strategy

import lxx.model.BattleState
import robocode.util.Utils
import java.lang.Math.signum
import lxx.math.*
import lxx.model.PointLike

class DuelStrategy : Strategy {

    override fun matches(battleState: BattleState) = true

    override fun getTurnDecision(battleState: BattleState) =
            TurnDecision(0.0, 0.0, getGunTurnAngle(battleState), 1.95, getRadarTurnAngleRadians(battleState))

    fun getGunTurnAngle(battleState: BattleState) =
            Utils.normalRelativeAngle(battleState.me.angleTo(battleState.enemy) - battleState.me.gunHeading) +
            RADIANS_10  - RADIANS_20 * Math.random()

    fun getRadarTurnAngleRadians(battleState : BattleState) : Double {
        val angleToTarget = battleState.me.angleTo(battleState.enemy)
        val sign: Double = when {
            angleToTarget != battleState.me.radarHeading -> signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
            else -> 1.0
        }
        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + RADIANS_10 * sign)
    }

}