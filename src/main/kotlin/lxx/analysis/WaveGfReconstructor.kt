package lxx.analysis

import lxx.model.*
import java.lang.Math.abs
import lxx.math.limit
import lxx.waves.WaveWithOffset
import java.lang.Math.signum

class WaveGfReconstructor(
        private val observerName: String,
        private val observableName: String
) : DataReconstructor<WaveWithOffset, Double, Double> {

    private val STOP_GF = 0.5
    private val MAX_SIDE_GF = 0.5

    override fun destruct(input: WaveWithOffset): Double {
        val maxEscapeAngle = input.wave.battleState.preciseMaxEscapeAngle(input.wave.victim, input.wave.speed)
        val latDir = lateralDirection(input.wave.center, input.wave.victim)
        val guessFactor = when {
            signum(latDir) == 0.0 && signum(input.offset) == 1.0 -> STOP_GF + abs(MAX_SIDE_GF * input.offset / maxEscapeAngle.forward)
            signum(latDir) == 0.0 && signum(input.offset) == -1.0 -> STOP_GF - abs(MAX_SIDE_GF * input.offset / maxEscapeAngle.backward)
            signum(input.offset) == signum(latDir) -> STOP_GF + abs(MAX_SIDE_GF * input.offset / maxEscapeAngle.forward)
            signum(input.offset) != signum(latDir) -> STOP_GF - abs(MAX_SIDE_GF * input.offset / maxEscapeAngle.backward)
            else -> throw IllegalArgumentException("Unexpected input offset ${input.offset}")
        }

        assert(guessFactor >= 0, "Guess factor ($guessFactor) is less than 0, hit offset = ${input.offset}, " +
                "mea.backward = ${maxEscapeAngle.backward}, mea.forward = ${maxEscapeAngle.forward}, mea.length = ${maxEscapeAngle.length()}")

        assert(guessFactor <= 2, "Guess factor ($guessFactor) is greater than 1, hit offset = ${input.offset}, " +
                "mea.backward = ${maxEscapeAngle.backward}, mea.forward = ${maxEscapeAngle.forward}, mea.length = ${maxEscapeAngle.length()}")

        return limit(0.0, guessFactor, 1.0)
    }

    [suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")]
    override fun reconstruct(battleState: BattleState, guessFactor: Double, bulletSpeed: Double): Double {
        assert(guessFactor >= 0.0 && guessFactor <= 1.0, "Guess factor = $guessFactor")

        val attacker = battleState.robotByName(observerName)
        val victim = battleState.robotByName(observableName)
        val maxEscapeAngle = battleState.preciseMaxEscapeAngle(victim, bulletSpeed)
        val zeroOffset = attacker.angleTo(victim)
        val bearingOffset = when {
            guessFactor == STOP_GF -> 0.0
            guessFactor > STOP_GF -> maxEscapeAngle.forward * ((guessFactor - STOP_GF) / MAX_SIDE_GF)
            guessFactor < STOP_GF -> maxEscapeAngle.backward * ((STOP_GF - guessFactor) / MAX_SIDE_GF)
            else -> throw IllegalArgumentException("Unexpected guess factor $guessFactor")
        }

        assert(bearingOffset >= maxEscapeAngle.minAngle - 0.01, "Bearing offset ($bearingOffset) is less than maxEscapeAngle, guessFactor=$guessFactor, " +
                "mea.length=${maxEscapeAngle.length()}, mea.forward=${maxEscapeAngle.forward}, mea.backward=${maxEscapeAngle.backward}, " +
                "zeroOffset=$zeroOffset")

        assert(bearingOffset <= maxEscapeAngle.maxAngle + 0.01, "Bearing offset ($bearingOffset) is grater than maxEscapeAngle, guessFactor=$guessFactor, " +
                "mea.length=${maxEscapeAngle.length()}, mea.forward=${maxEscapeAngle.forward}, mea.backward=${maxEscapeAngle.backward}, " +
                "zeroOffset=$zeroOffset")

        return limit(maxEscapeAngle.minAngle, bearingOffset, maxEscapeAngle.maxAngle)
    }

}