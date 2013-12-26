package lxx.movement.mech

import lxx.model.LxxRobot
import lxx.movement.MovementDecision

public trait MovementMechanics<DESTINATION> {

    fun getMovementDecision(me: LxxRobot, destination: DESTINATION): MovementDecision

}