package lxx.strategy

import lxx.model.BattleState

interface Strategy {

    fun matches(battleState: BattleState): Boolean

    fun getTurnDecision(battleState: BattleState): TurnDecision

}