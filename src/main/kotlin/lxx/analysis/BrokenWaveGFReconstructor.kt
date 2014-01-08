package lxx.analysis

import lxx.waves.BrokenWave
import lxx.model.*
import java.lang.Math.abs
import lxx.math.limit

class BrokenWaveGFReconstructor(
        private val observerName: String,
        private val observableName: String
) : DataReconstructor<BrokenWave, Double, Double> {

    override fun destruct(input: BrokenWave): Double {
        val maxEscapeAngle = getMaxEscapeAngle(input.wave.attacker, input.wave.victim, input.wave.speed)
        val guessFactor = abs(input.hitOffset - maxEscapeAngle.backward) / maxEscapeAngle.length()

        assert(guessFactor >= 0, "Guess factor ($guessFactor) is less than 0, hit offset = ${input.hitOffset}, " +
        "mea.backward = ${maxEscapeAngle.backward}, mea.forward = ${maxEscapeAngle.forward}, mea.length = ${maxEscapeAngle.length()}")

        assert(guessFactor <= 1, "Guess factor ($guessFactor) is greater than 1, hit offset = ${input.hitOffset}, " +
        "mea.backward = ${maxEscapeAngle.backward}, mea.forward = ${maxEscapeAngle.forward}, mea.length = ${maxEscapeAngle.length()}")

        return limit(0.0, guessFactor, 1.0)
    }

    [suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")]
    override fun reconstruct(battleState: BattleState, guessFactor: Double, bulletSpeed: Double): Double {
        assert(guessFactor >= 0.0 && guessFactor <= 1.0, "Guess factor = $guessFactor")

        val attacker = battleState.robotByName(observerName)
        val victim = battleState.robotByName(observableName)
        val maxEscapeAngle = getMaxEscapeAngle(attacker, victim, bulletSpeed)
        val zeroOffset = attacker.angleTo(victim)
        val bearingOffset = guessFactor * maxEscapeAngle.length() * Math.signum(maxEscapeAngle.forward) + maxEscapeAngle.backward

        assert(bearingOffset >= maxEscapeAngle.minAngle, "Bearing offset ($bearingOffset) is less than minEscapeAngle, guessFactor=$guessFactor, " +
        "mea.length=${maxEscapeAngle.length()}, mea.forward=${maxEscapeAngle.forward}, mea.backward=${maxEscapeAngle.backward}, " +
        "zeroOffset=$zeroOffset")

        assert(bearingOffset <= maxEscapeAngle.maxAngle, "Bearing offset ($bearingOffset) is grater than minEscapeAngle, guessFactor=$guessFactor, " +
        "mea.length=${maxEscapeAngle.length()}, mea.forward=${maxEscapeAngle.forward}, mea.backward=${maxEscapeAngle.backward}, " +
        "zeroOffset=$zeroOffset")

        return limit(maxEscapeAngle.minAngle, bearingOffset, maxEscapeAngle.maxAngle)
    }

}