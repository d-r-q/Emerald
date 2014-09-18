package lxx.model

import lxx.math.*
import lxx.math.QuickMath.NaN
import java.lang.Math.*
import robocode.Rules
import lxx.util.Logger
import lxx.movement.MovementDecision
import robocode.util.Utils

private val UNKNOWN = "Unknown"

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
        val acceleration: Double,
        val advancedRobot: Boolean
) : PointLike {

    val speed = abs(velocity)

    fun turnsToFire() = gunHeat / battleRules.gunCoolingRate

    fun distanceToForwardWall() =
            battleRules.battleField.getDistanceToWall(battleRules.battleField.getWall(this, heading), this)

    override fun x() = x
    override fun y() = y

    fun apply(movementDecision: MovementDecision): LxxRobot {
        val velocity = newVelocity(velocity, movementDecision.movementDirection)
        val maxTurnRate = Rules.getTurnRateRadians(this.velocity)
        val turnRate = limit(-maxTurnRate, movementDecision.turnRate, maxTurnRate)
        val heading = Utils.normalAbsoluteAngle(heading + turnRate)
        val (x, y) = project(heading, velocity)
        return this.copy(time = time + 1, prevState = this,
                x = x, y = y,
                velocity = velocity, heading = heading, acceleration = calculateAcceleration(this, velocity),
                gunHeat = max(0.0, gunHeat - battleRules.gunCoolingRate))
    }

}

data class LxxRobotBuilder(
        val prevState: LxxRobot? = null,
        var name: String = prevState?.name ?: UNKNOWN,
        var alive: Boolean = prevState?.alive ?: true,
        var energy: Double = prevState?.energy ?: NaN,
        var time: Long = prevState?.time ?: -999,
        var lastScanTime: Long = prevState?.lastScanTime ?: -999,
        var x: Double = prevState?.x ?: NaN,
        var y: Double = prevState?.y ?: NaN,
        var velocity: Double = prevState?.velocity ?: NaN,
        var heading: Double = prevState?.heading ?: NaN,
        var gunHeading: Double = prevState?.gunHeading ?: NaN,
        var radarHeading: Double = prevState?.radarHeading ?: NaN,
        var gunHeat: Double = NaN,
        var firePower: Double? = null,
        var givenDamage: Double = 0.0,
        var takenDamage: Double = 0.0,
        var advancedRobot: Boolean = prevState?.advancedRobot ?: false
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
            calculateGunHeat(battleRules), calculateFirePower(battleRules),
            calculateAcceleration(prevState, velocity), isAdvancedRobot(battleRules))

    private fun calculateGunHeat(battleRules: BattleRules): Double {
        if (!gunHeat.isNaN()) {
            return gunHeat
        } else if (prevState == null) {
            return battleRules.initialGunHeat - (battleRules.gunCoolingRate * time)
        }

        val prevGunHeat = if (prevState.gunHeat.isNaN()) battleRules.initialGunHeat else prevState.gunHeat

        return max(0.0, prevGunHeat - (time - prevState.time) * battleRules.gunCoolingRate)
    }

    private fun calculateFirePower(battleRules: BattleRules): Double {
        if (firePower != null) {
            return firePower!!
        }

        if (prevState == null || prevState.name == UNKNOWN) {
            return 0.0
        }

        val expectedEnergy = prevState.energy - takenDamage - wallDamage(battleRules) + givenDamage
        val energyDiff = expectedEnergy - energy
        if (energyDiff < 0.1) {
            return 0.0
        }
        if (energy < expectedEnergy) {
            Logger.debug({ "$name fire with power ${energyDiff}" })
        }
        return limit(0.1, energyDiff, 3.0)
    }

    private fun wallDamage(battleRules: BattleRules): Double {
        if (prevState == null || !isAdvancedRobot(battleRules)) {
            return 0.0
        }
        val expectedPos = prevState.project(heading, prevState.velocity)
        if (velocity == 0.0 && !battleRules.battleField.availableRect.contains(expectedPos.x, expectedPos.y)) {
            return Rules.getWallHitDamage(limit(0.0, abs(prevState.velocity + prevState.acceleration), Rules.MAX_VELOCITY))
        } else {
            return 0.0
        }
    }

    private fun isAdvancedRobot(battleRules: BattleRules): Boolean {
        val behavesLikeAdvanced = known() && movingAndTurning() && notHitWall(battleRules) && noSkippedScans()

        if (!advancedRobot && behavesLikeAdvanced) {
            Logger.info({ "Enemy recognized as AdvancedRobot" })
        }

        return advancedRobot || behavesLikeAdvanced
    }

    private fun known() = prevState != null && prevState.name != UNKNOWN

    private fun noSkippedScans() = prevState != null && time == prevState.time + 1

    private fun notHitWall(battleRules: BattleRules) = velocity != 0.0 ||
            (prevState != null && battleRules.battleField.notNearWall(prevState))

    private fun movingAndTurning() = prevState != null && velocity != prevState.velocity && heading != prevState.heading

    override fun x() = x
    override fun y() = y

}