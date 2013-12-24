package lxx

import java.lang.Double as JDouble
import robocode.RobotStatus
import robocode.ScannedRobotEvent
import robocode.StatusEvent
import lxx.model.BattleField
import lxx.model.BattleRules

val stdRules = BattleRules(BattleField(18.0, 18.0, 764.0, 564.0), 36.0, 36.0, 0.1, 3.0, "")

fun RobotStatus(
        energy: Double = JDouble.NaN,
        x: Double = JDouble.NaN,
        y: Double = JDouble.NaN,
        bodyHeading: Double = JDouble.NaN,
        gunHeading: Double = JDouble.NaN,
        radarHeading: Double = JDouble.NaN,
        velocity: Double = JDouble.NaN,
        bodyTurnRemaining: Double = JDouble.NaN,
        radarTurnRemaining: Double = JDouble.NaN,
        gunTurnRemaining: Double = JDouble.NaN,
        distanceRemaining: Double = JDouble.NaN,
        gunHeat: Double = JDouble.NaN,
        others: Int = -1,
        roundNum: Int = -1,
        numRounds: Int = -1,
        time: Long = -1L
): RobotStatus {
    val javaClass = javaClass<RobotStatus>()
    val constructor = javaClass.getDeclaredConstructors().find { it.getParameterTypes()!!.size == 16 }
    constructor!!.setAccessible(true)

    return constructor.newInstance(
            energy, x, y, bodyHeading, gunHeading, radarHeading, velocity, bodyTurnRemaining, radarTurnRemaining,
            gunTurnRemaining, distanceRemaining, gunHeat, others, roundNum, numRounds, time) as RobotStatus


}