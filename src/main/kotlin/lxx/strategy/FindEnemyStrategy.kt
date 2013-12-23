package lxx.strategy

import lxx.model.BattleState
import robocode.Rules
import robocode.util.Utils

class FindEnemyStrategy : Strategy {

    override fun matches(battleState: BattleState) =
            battleState.opponent.alive && battleState.time - battleState.opponent.lastScanTime >= 3

    override fun getTurnDecision(battleState: BattleState): TurnDecision {
        val turnDirection = Math.signum(Utils.normalRelativeAngle(battleState.me.angleTo(battleState.opponent) - battleState.me.radarHeading))
        return TurnDecision(0.0, Rules.MAX_TURN_RATE_RADIANS * turnDirection, Rules.GUN_TURN_RATE_RADIANS * turnDirection, 0.0, Rules.RADAR_TURN_RATE_RADIANS * turnDirection)
    }

}