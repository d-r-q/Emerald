package lxx

import robocode.RobotStatus
import robocode.ScannedRobotEvent
import lxx.model.BattleField
import lxx.model.BattleRules
import lxx.math.QuickMath.NaN

val stdRules = BattleRules(BattleField(800.0, 600.0, 18.0), 36.0, 36.0, 0.1, 3.0, "me", "enemy")

fun RobotStatus(
        energy: Double = NaN,
        x: Double = NaN,
        y: Double = NaN,
        bodyHeading: Double = NaN,
        gunHeading: Double = NaN,
        radarHeading: Double = NaN,
        velocity: Double = NaN,
        bodyTurnRemaining: Double = NaN,
        radarTurnRemaining: Double = NaN,
        gunTurnRemaining: Double = NaN,
        distanceRemaining: Double = NaN,
        gunHeat: Double = NaN,
        others: Int = -1,
        numSentries: Int = -1,
        roundNum: Int = -1,
        numRounds: Int = -1,
        time: Long = -1L
): RobotStatus {
    val javaClass = javaClass<RobotStatus>()
    val constructor = javaClass.getDeclaredConstructors().firstOrNull() { it.getParameterTypes()!!.size == 17 }
    constructor!!.setAccessible(true)
    x.isNaN()
    return constructor.newInstance(
            energy, x, y, bodyHeading, gunHeading, radarHeading, velocity, bodyTurnRemaining, radarTurnRemaining,
            gunTurnRemaining, distanceRemaining, gunHeat, others, numSentries, roundNum, numRounds, time) as RobotStatus


}

fun ScannedRobotEvent(name: String = "Test", distance: Double = 1.0): ScannedRobotEvent =
        ScannedRobotEvent(name, 0.0, 0.0, distance, 0.0, 0.0)