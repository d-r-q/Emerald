package lxx.movement

import lxx.model.BattleState

interface Movement {

    fun getMovementDecision(battleState: BattleState): MovementDecision

}