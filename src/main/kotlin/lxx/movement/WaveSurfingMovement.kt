package lxx.movement

import lxx.analysis.Collector
import lxx.model.BattleState
import lxx.model.BattleRules
import lxx.waves.LxxWave
import robocode.Rules
import lxx.paint.Canvas
import java.awt.Color
import ags.utils.KdTree
import lxx.model.LxxRobot
import java.lang.Math.abs
import lxx.model.lateralVelocity
import lxx.model.advancingVelocity
import lxx.analysis.WaveDataCollector
import lxx.analysis.WaveGfReconstructor
import lxx.analysis.Profile
import lxx.model.PointLike
import lxx.movement.mech.OrbitalMovementMech
import lxx.movement.mech.GoToMovementMech
import java.util.HashMap
import lxx.movement.mech.futurePositions
import lxx.movement.mech.OrbitDestination
import lxx.movement.mech.OrbitDirection
import robocode.util.Utils
import lxx.waves.RealWavesWatcher
import lxx.waves.Wave

class WaveSurfingMovement(battleRules: BattleRules) : Collector, Movement {

    private val orbMov = OrbitalMovementMech(battleRules.battleField, 300.0)

    private val goToMov = GoToMovementMech()

    private val destinations: HashMap<LxxWave, Pair<PointLike, Profile>> = hashMapOf()

    companion object {

        val tree = KdTree.SqrEuclid<Double>(5, 1200)

        val locFormula = { observer: LxxRobot, observable: LxxRobot ->
            doubleArrayOf(observable.acceleration + 2 / 3 * 2,
                    abs(lateralVelocity(observer, observable)) / Rules.MAX_VELOCITY * 4,
                    advancingVelocity(observer, observable) / Rules.MAX_VELOCITY * 3,
                    observer.distance(observable) / 800,
                    observable.distanceToForwardWall() / 800 * 2)
        }

    }

    private val wavesWatcher = RealWavesWatcher(battleRules.enemyName, battleRules.myName)

    private val dataCollector = WaveDataCollector(locFormula,
            WaveGfReconstructor(battleRules.enemyName, battleRules.myName),
            tree, battleRules.enemyName, battleRules.myName, wavesWatcher.hitWavesStream())

    private val passedWaves = wavesWatcher.brokenWavesStream()

    override fun getMovementDecision(battleState: BattleState): MovementDecision {
        val wave = wavesWatcher.wavesInAir.
                sortedBy { it.flightTime(battleState.me) }.
                firstOrNull { it.flightTime(battleState.me) > 3 }

        if (wave != null) {
            val (dest, profile) = destinations.getOrPut(wave, { destination(battleState, wave) })

            profile.drawCurrentBo(Canvas.MY_MOVEMENT_PROFILE, wave.toBearingOffset(battleState.me))

            return goToMov.getMovementDecision(battleState.me, dest)

        } else {

            val bf = battleState.battleField
            val dir = {
                val curAngle = bf.center.angleTo(battleState.me)
                val desiredAngle = Utils.normalAbsoluteAngle(bf.center.angleTo(battleState.enemy) + lxx.math.RADIANS_180)
                val anglesDiff = Utils.normalRelativeAngle(desiredAngle - curAngle)
                if (abs(anglesDiff) < lxx.math.RADIANS_10) OrbitDirection.STOP
                else if (anglesDiff < 0.0) OrbitDirection.COUNTER_CLOCKWISE
                else if (anglesDiff > 0.0) OrbitDirection.CLOCKWISE
                else throw IllegalArgumentException("anglesDiff = $anglesDiff")
            }
            return orbMov.getMovementDecision(battleState.me, OrbitDestination(bf.center, dir()))
        }

    }

    override fun collectData(battleState: BattleState) {
        wavesWatcher.collectData(battleState)
        dataCollector.collectData(battleState)
        passedWaves.forEach { destinations.remove(it as Wave) }
    }

    private fun destination(currentState: BattleState, wave: LxxWave): Pair<PointLike, Profile> {
        val data = dataCollector.getData(wave.battleState, wave.speed)
        val mea = wave.battleState.preciseMaxEscapeAngle(wave.victim, wave.speed)
        val profile = Profile(data, mea.minAngle, mea.maxAngle, width = 35)

        val (cwPoints, ccwPoints) = futurePositions(currentState.me, wave, 800.0)

        val canvas = Canvas.PREDICTED_POSITIONS
        if (canvas.enabled) {
            canvas.reset()

            canvas.setColor(Color.BLUE)
            cwPoints.forEach { canvas.drawCircle(it, 2.0) }

            canvas.setColor(Color.GREEN)
            ccwPoints.forEach { canvas.drawCircle(it, 2.0) }

        }

        canvas.setColor(Color(255, 200, 0, 155))
        val (wcw, wccw) = futurePositions(wave.victim, wave, wave.distance(wave.victim))
        (wcw + wccw).forEach { canvas.drawCircle(it, 2.0) }

        profile.drawProfile(Canvas.MY_MOVEMENT_PROFILE)
        profile.drawCurrentBo(Canvas.MY_MOVEMENT_PROFILE, wave.toBearingOffset(currentState.me))

        fun pointDanger(pnt: PointLike) = profile.bearingOffsetDanger(wave.toBearingOffset(pnt))
        val dest = (cwPoints + ccwPoints).minBy { pointDanger(it) }!!

        canvas.setColor(Color.RED)
        canvas.drawCircle(dest, 3.0)

        return Pair(dest, profile)
    }

}