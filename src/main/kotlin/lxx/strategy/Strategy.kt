package lxx.strategy

import lxx.model.BattleState

trait Strategy {

    fun matches(battleState: BattleState): Boolean

    fun getTurnDecision(battleState: BattleState): TurnDecision

}