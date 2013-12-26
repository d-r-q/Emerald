package lxx

import robocode.AdvancedRobot
import robocode.ScannedRobotEvent
import robocode.util.Utils
import java.lang.Math.*
import java.awt.geom.Point2D

public class OrbitingRobot : AdvancedRobot() {

    val direction = if (Math.random() < 0.5) -1 else 1
    val speed = 24 * Math.random()
    var distance = 100.0

    override fun run() {
        distance = 100 + (Math.min(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2) - 100 - getWidth() / 2) * Math.random()
        setTurnRadarRightRadians(java.lang.Double.POSITIVE_INFINITY)

        while (true) {
            setDebugProperty("direction", direction.toString())
            setDebugProperty("speed", speed.toString())
            setDebugProperty("distance", distance.toString())
            if (getRadarTurnRemaining() == 0.0) {
                setTurnRadarRightRadians(java.lang.Double.POSITIVE_INFINITY)
            }
            execute()
        }
    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        val angleToEnemy = getHeadingRadians() + event!!.getBearingRadians()
        val enemyX = getX() + Math.sin(angleToEnemy) * event.getDistance()
        val enemyY = getY() + Math.cos(angleToEnemy) * event.getDistance()
        val distanceBetween = Point2D.distance(getX(), getY(), enemyX, enemyY)

        turnRadar(angleToEnemy)
        move(angleToEnemy, distanceBetween)
    }

    private fun turnRadar(angleToEnemy: Double) {
        val radarHeading = getRadarHeadingRadians()
        val sign: Double = when {
            angleToEnemy != radarHeading -> signum(Utils.normalRelativeAngle(angleToEnemy - radarHeading))
            else -> 1.0
        }
        setTurnRadarRightRadians(Utils.normalRelativeAngle(angleToEnemy - radarHeading + Math.toRadians(10.0) * sign))
    }

    private fun move(angleToEnemy: Double, distanceBetween: Double) {
        val distanceDiff = distanceBetween - distance
        val attackAngleKoeff = Math.min(Math.max(distanceDiff / distanceBetween, 0.0), 1.0)

        val angleToMe = Utils.normalAbsoluteAngle(angleToEnemy + Math.PI)
        val desiredHeading = angleToMe + (Math.PI / 2 + Math.PI / 2 * attackAngleKoeff) * direction
        setTurnRightRadians(Utils.normalRelativeAngle(desiredHeading - getHeadingRadians()))
        setAhead(speed)
    }

}