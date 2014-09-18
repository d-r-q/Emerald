package lxx.strategy

import lxx.model.BattleState
import robocode.util.Utils
import java.lang.Math.signum
import lxx.math.*
import lxx.gun.main.MainGun
import lxx.movement.WaveSurfingMovement

class DuelStrategy(private val gun: MainGun,
                   private val movement: WaveSurfingMovement) : Strategy {

    override fun matches(battleState: BattleState) = battleState.enemy.alive

    override fun getTurnDecision(battleState: BattleState): TurnDecision {
        val movementDecision = movement.getMovementDecision(battleState)
        val gunDecision = gun.getTurnDecision(battleState)
        return TurnDecision(movementDecision.movementDirection, movementDecision.turnRate,
                gunDecision.gunTurnAngle, gunDecision.firePower ?: 0.0,
                getRadarTurnAngleRadians(battleState))
    }

    fun getGunTurnAngle(battleState: BattleState) =
            Utils.normalRelativeAngle(battleState.me.angleTo(battleState.enemy) - battleState.me.gunHeading) +
            if (battleState.me.gunHeat <= battleState.rules.gunCoolingRate) RADIANS_10 - RADIANS_20 * Math.random()
            else 0.0

    fun getRadarTurnAngleRadians(battleState: BattleState): Double {
        val angleToTarget = battleState.me.angleTo(battleState.enemy)
        val sign: Double = when {
            angleToTarget != battleState.me.radarHeading -> signum(Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading))
            else -> 1.0
        }
        return Utils.normalRelativeAngle(angleToTarget - battleState.me.radarHeading + RADIANS_10 * sign)
    }

}