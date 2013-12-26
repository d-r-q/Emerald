package lxx.movement

import lxx.model.BattleState

public trait Movement {

    fun getMovementDecision(battleState: BattleState): MovementDecision

}