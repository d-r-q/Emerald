package lxx.movement

import lxx.model.BattleState
import lxx.movement.mech.GoToMovementMech

class StayAtCenterMovement : Movement {

    private val movementMech = GoToMovementMech()

    override fun getMovementDecision(battleState: BattleState)=
            movementMech.getMovementDecision(battleState.me, battleState.battleField.center)

}