package lxx.movement.mech

import lxx.model.BattleField
import lxx.model.LxxRobot
import lxx.model.PointLike
import lxx.movement.MovementDecision
import robocode.util.Utils.*
import lxx.math.*
import lxx.waves.Wave

public enum class OrbitDirection(val direction: Int, val speed: Int) {

    CLOCKWISE : OrbitDirection(1, 24)
    COUNTER_CLOCKWISE : OrbitDirection(-1, 24)
    STOP : OrbitDirection(0, 0)

}

public data class OrbitDestination(val center: PointLike, val direction: OrbitDirection)

public class OrbitalMovementMech(val battleField: BattleField, val desiredDistance: Double) : MovementMechanics<OrbitDestination> {

    public override fun getMovementDecision(me: LxxRobot, destination: OrbitDestination): MovementDecision {
        val desiredHeading: Double
        val smoothedHeading: Double
        if (destination.direction.speed != 0) {
            desiredHeading = getDesiredHeading(me, destination)
            smoothedHeading = battleField.smoothWalls(me, desiredHeading, destination.direction.direction == 1)
        } else {
            desiredHeading = normalAbsoluteAngle(destination.center.angleTo(me) + RADIANS_90)
            smoothedHeading = desiredHeading
        }
        return toMovementDecision(me, destination.direction.speed.toDouble(), smoothedHeading)
    }

    private fun getDesiredHeading(me: LxxRobot, destination: OrbitDestination): Double {
        val distanceBetween = me.distance(destination.center)
        val distanceDiff = distanceBetween - desiredDistance
        val attackAngleKoeff = distanceDiff / desiredDistance
        val attackAngle = RADIANS_90 + (RADIANS_90 * attackAngleKoeff)
        val angleToMe = angle(destination.center.x(), destination.center.y(), me.x, me.y)
        return normalAbsoluteAngle(angleToMe + attackAngle * destination.direction.direction)
    }

}

fun futurePositions(initState: LxxRobot, wave: Wave, desiredDistance: Double): Pair<List<PointLike>, List<PointLike>> {
    val orbMech = OrbitalMovementMech(initState.battleRules.battleField, desiredDistance)
    val cwPos = stream(initState, pointsGenerator(OrbitDestination(wave, OrbitDirection.CLOCKWISE), orbMech)).
            takeWhile{!wave.isReached(it)}.
            toList()
    val ccwPos = stream(initState, pointsGenerator(OrbitDestination(wave, OrbitDirection.COUNTER_CLOCKWISE), orbMech)).
            takeWhile {!wave.isReached(it)}.
            toList()

    return Pair(cwPos, ccwPos)
}

fun pointsGenerator(orbitDestination: OrbitDestination, orbMov: OrbitalMovementMech) = {(robot: LxxRobot) ->
    val movementDecision = orbMov.getMovementDecision(robot, orbitDestination)
    robot.apply(movementDecision)
}