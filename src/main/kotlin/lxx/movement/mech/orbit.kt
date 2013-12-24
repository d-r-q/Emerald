package lxx.movement.mech

import lxx.model.BattleField
import lxx.model.LxxRobot
import lxx.model.PointLike
import lxx.movement.MovementDecision
import robocode.util.Utils.*
import lxx.math.*

public enum class OrbitDirection(val direction: Int, val speed: Int) {

    CLOCKWISE : OrbitDirection(1, 8)
    COUNTER_CLOCKWISE : OrbitDirection(-1, 8)
    STOP : OrbitDirection(0, 0)

}

public class OrbitalMovement(val battleField: BattleField, val desiredDistance: Double) {

    public fun getMovementDecision(me: LxxRobot, center: PointLike, direction: OrbitDirection, minAttackAngle: Double): MovementDecision {
        val desiredHeading: Double
        val smoothedHeading: Double
        if (direction.speed != 0) {
            desiredHeading = getDesiredHeading(me, center, direction, minAttackAngle)
            smoothedHeading = battleField.smoothWalls(me, desiredHeading, direction.direction == 1)
        } else {
            desiredHeading = normalAbsoluteAngle(center.angleTo(me) + RADIANS_90)
            smoothedHeading = desiredHeading
        }
        return toMovementDecision(me, direction.speed, smoothedHeading)
    }

    private fun toMovementDecision(robot: LxxRobot, desiredSpeed: Int, desiredHeading: Double): MovementDecision {
        val wantToGoFront = anglesDiff(robot.heading, desiredHeading) < RADIANS_90
        val normalizedDesiredHeading =
                (if (wantToGoFront) desiredHeading
                else normalAbsoluteAngle(desiredHeading + RADIANS_180)
                )

        val turnRemaining = normalRelativeAngle(normalizedDesiredHeading - robot.heading)
        val direction = desiredSpeed *
                (if (wantToGoFront) 1
                else -1).toDouble()

        return MovementDecision(direction, turnRemaining)
    }

    private fun getDesiredHeading(me: LxxRobot, center: PointLike, direction: OrbitDirection, minAttackAngle: Double): Double {
        val distanceBetween = me.distance(center)
        val distanceDiff = distanceBetween - desiredDistance
        val attackAngleKoeff = distanceDiff / desiredDistance
        val attackAngle = RADIANS_90 + (RADIANS_90 * attackAngleKoeff)
        val angleToMe = angle(center.x(), center.y(), me.x, me.y)
        return normalAbsoluteAngle(angleToMe + limit(minAttackAngle, attackAngle, RADIANS_100) * direction.direction)
    }

}
