package lxx.strategy

import lxx.model.BattleState
import robocode.util.Utils.*

class WinStrategy : Strategy {

    override fun matches(battleState: BattleState) = true

    override fun getTurnDecision(battleState: BattleState) =
            TurnDecision(0.0, normalRelativeAngle(-battleState.me.heading),
                    normalRelativeAngle(-battleState.me.gunHeading), 0.0,
                    -battleState.me.radarHeading)

}