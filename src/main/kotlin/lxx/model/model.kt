package lxx.model

import lxx.math.QuickMath
import robocode.Rules
import robocode.util.Utils
import java.lang.Math.signum
import lxx.math.*

fun lateralDirection(center : PointLike, robot : LxxRobot) = lateralDirection(center, robot, robot.velocity, robot.heading)

fun lateralDirection(center : PointLike, pos : PointLike, velocity : Double, heading : Double) : Double {
    assert(!java.lang.Double.isNaN(heading))
    if (Utils.isNear(0.0, velocity)) {
        return 1.0
    }

    return signum(lateralVelocity(center, pos, velocity, heading))
}

fun lateralVelocity(center : PointLike, robot : LxxRobot) = lateralVelocity(center, robot, robot.velocity, robot.heading)

fun lateralVelocity(center : PointLike, pos : PointLike, velocity : Double, heading : Double) : Double {
    assert(!java.lang.Double.isNaN(heading))
    assert(heading >= 0 && heading <= RADIANS_360)
    return velocity * QuickMath.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)))
}

fun advancingVelocity(center : PointLike, robot : LxxRobot) = advancingVelocity(center, robot, robot.velocity, robot.heading)

fun advancingVelocity(center : PointLike, pos : PointLike, velocity : Double, heading : Double) : Double {
    assert(!java.lang.Double.isNaN(heading))
    assert(heading >= 0 && heading <= RADIANS_360)
    return velocity * QuickMath.cos(Utils.normalRelativeAngle(heading - center.angleTo(pos)))
}

fun getMaxEscapeAngle(attackerPos: PointLike, victim: LxxRobot, bulletSpeed: Double) : MaxEscapeAngle {
    val possibleMea = QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed) * 1.1
    val lateralDirection = lateralDirection(attackerPos, victim)
    if (lateralDirection >= 0) {
        return MaxEscapeAngle(-possibleMea, possibleMea)
    } else {
        return MaxEscapeAngle(possibleMea, -possibleMea)
    }
}

fun getStopDistance(speed : Double) : Double {
    assert(speed >= 0 && speed <= Rules.MAX_VELOCITY)
    var currentSpeed = speed
    var distance = 0.0
    while (currentSpeed > 0) {
        currentSpeed -= Rules.DECELERATION
        distance += currentSpeed
    }
    assert(distance <= 6 + 4 + 2)
    return distance
}

fun getAcceleratedSpeed(speed: Double): Double {
    assert(speed >= 0)
    assert(speed <= Rules.MAX_VELOCITY)
    return limit(0.0, speed + Rules.ACCELERATION, Rules.MAX_VELOCITY)
}