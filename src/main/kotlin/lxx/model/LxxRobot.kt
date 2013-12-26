package lxx.model

import java.lang.Double as JDouble
import lxx.math.*
import java.lang.Math.abs
import java.lang.Math.signum
import robocode.Rules

data class LxxRobot(
        val battleRules: BattleRules,
        val prevState: LxxRobot?,
        val name: String,
        val alive: Boolean,
        val energy: Double,
        val time: Long,
        val lastScanTime: Long,
        val x: Double,
        val y: Double,
        val velocity: Double,
        val heading: Double,
        val gunHeading: Double,
        val radarHeading: Double,
        val gunHeat: Double,
        val firePower: Double?,
        val acceleration: Double
        ) : PointLike {

    val speed = abs(velocity)

    fun turnsToFire() = gunHeat / battleRules.gunCoolingRate

    fun distanceToForwardWall() =
            battleRules.battleField.getDistanceToWall(battleRules.battleField.getWall(this, heading), this)

    override fun x() = x
    override fun y() = y

}

data class LxxRobotBuilder(
        val prevState: LxxRobot? = null,
        var name: String = prevState?.name ?: "Unknown",
        var alive: Boolean = prevState?.alive ?: true,
        var energy: Double = prevState?.energy ?: JDouble.NaN,
        var time: Long = prevState?.time ?: -999,
        var lastScanTime: Long = prevState?.lastScanTime ?: -999,
        var x: Double = prevState?.x ?: JDouble.NaN,
        var y: Double = prevState?.y ?: JDouble.NaN,
        var velocity: Double = prevState?.velocity ?: JDouble.NaN,
        var heading: Double = prevState?.heading ?: JDouble.NaN,
        var gunHeading: Double = prevState?.gunHeading ?: JDouble.NaN,
        var radarHeading: Double = prevState?.radarHeading ?: JDouble.NaN,
        var gunHeat: Double = prevState?.gunHeading ?: JDouble.NaN,
        var firePower: Double? = null
) : PointLike {

    fun with(newName: String = name,
             newAlive: Boolean = alive,
             newEnergy: Double = energy,
             newTime: Long = time,
             newLastScanTime: Long = lastScanTime,
             newX: Double = x,
             newY: Double = y,
             newVelocity: Double = velocity,
             newHeading: Double = heading,
             newGunHeading: Double = gunHeading,
             newRadarHeading: Double = radarHeading,
             newGunHeat: Double = gunHeat,
             newFirePower: Double? = firePower): LxxRobotBuilder {

        name = newName
        alive = newAlive
        energy = newEnergy
        time = newTime
        lastScanTime = newLastScanTime
        x = newX
        y = newY
        velocity = newVelocity
        heading = newHeading
        gunHeading = newGunHeading
        radarHeading = newRadarHeading
        gunHeat = newGunHeat
        firePower = newFirePower

        return this
    }

    fun build(battleRules: BattleRules) = LxxRobot(
            battleRules,
            prevState,
            name, alive, energy, time, lastScanTime,
            x, y,
            velocity,
            heading, gunHeading, radarHeading,
            gunHeat, firePower,
            calculateAcceleration(prevState, velocity))

    private fun calculateAcceleration(prevStateOption : LxxRobot?, velocity : Double) : Double {
        if (prevStateOption == null) {
            return 0.0
        }

        val prevState = prevStateOption!!
        var acceleration : Double
        if (sameDirection(velocity, prevState)) {
            acceleration = abs(velocity) - abs(prevState.velocity)
        } else {
            acceleration = abs(velocity)
        }
        if (prevState.time != prevState.lastScanTime) {
            acceleration = limit(-Rules.DECELERATION, acceleration, Rules.ACCELERATION)
        }

        return acceleration
    }

    private fun sameDirection(velocity : Double, prevState : LxxRobot) : Boolean {
        return signum(velocity) == signum(prevState.velocity) || abs(velocity) < EPSILON
    }

    override fun x() = x
    override fun y() = y

}