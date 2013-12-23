package lxx.strategy

import lxx.model.BattleState
import robocode.util.Utils
import java.lang.Math.signum
import lxx.math.*

class DuelStrategy : Strategy {

    override fun matches(battleState: BattleState) = true

    override fun getTurnDecision(battleState: BattleState)=
            TurnDecision(0.0, 0.0, 0.0, 0.0, getRadarTurnAngleRadians(battleState))

    fun getRadarTurnAngleRadians(battleState : BattleState) : Double {
        val angleToTarget = battleState.me.angleTo(battleState.opponent)
        val sign: Double = when {
            angleToTarget != battleState.me.radarHeading -> signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
            else -> 1.0
        }
        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + RADIANS_10 * sign)
    }


}