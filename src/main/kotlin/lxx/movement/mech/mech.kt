package lxx.movement.mech

import lxx.model.LxxRobot
import lxx.math.*
import lxx.movement.MovementDecision
import robocode.util.Utils.*

fun wantToGoFront(robot: LxxRobot, desiredHeading: Double) = anglesDiff(robot.heading, desiredHeading) < RADIANS_90

fun toMovementDecision(robot: LxxRobot, desiredSpeed: Double, desiredHeading: Double): MovementDecision {
    val normalizedDesiredHeading =
            if (wantToGoFront(robot, desiredHeading)) desiredHeading
            else normalAbsoluteAngle(desiredHeading + RADIANS_180)

    val turnRemaining = normalRelativeAngle(normalizedDesiredHeading - robot.heading)
    val direction = desiredSpeed *
    if (wantToGoFront(robot, desiredHeading)) 1.0
    else -1.0

    return MovementDecision(direction, turnRemaining)
}