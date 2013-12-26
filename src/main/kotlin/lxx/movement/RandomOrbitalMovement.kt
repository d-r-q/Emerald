package lxx.movement

import lxx.movement.mech.OrbitDirection
import lxx.model.BattleState
import lxx.movement.mech.OrbitalMovementMech
import lxx.model.BattleField
import lxx.math.*
import lxx.movement.mech.OrbitDirection.STOP
import lxx.movement.mech.OrbitDestination

class RandomOrbitalMovement(battleField: BattleField) : Movement {

    private var dir = randomDir()
    private var untilTick = randomUntilTick()
    private val orbitalMovement = OrbitalMovementMech(battleField, 300.0)

    override fun getMovementDecision(battleState: BattleState): MovementDecision {
        if (battleState.time >= untilTick) {
            dir = randomDir()
            untilTick = battleState.time + randomUntilTick()
        }
        return orbitalMovement.getMovementDecision(battleState.me, OrbitDestination(battleState.enemy, dir))
    }

    private fun randomDir() =
            if (Math.random() < 0.15) OrbitDirection.STOP
            else if (Math.random() < 0.5) OrbitDirection.CLOCKWISE
            else OrbitDirection.COUNTER_CLOCKWISE

    private fun randomUntilTick() = 5L + (16 * Math.random()).toInt()
}