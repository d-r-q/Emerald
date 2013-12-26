package lxx.movement.mech

import lxx.model.PointLike
import lxx.model.LxxRobot
import lxx.movement.MovementDecision
import lxx.model.*

class GoToMovementMech : MovementMechanics<PointLike> {

    override fun getMovementDecision(me: LxxRobot, destination: PointLike): MovementDecision {
        var distance = me.distance(destination)
        if (distance < 0.1) {
            distance = 0.0
        }
        val desiredSpeed =
                if (distance < getStopDistance(me.speed)) 0.0
                else distance

        val desiredHeading =
                if (desiredSpeed > 0) me.angleTo(destination)
                else me.heading

        return toMovementDecision(me, desiredSpeed, desiredHeading)
    }

}