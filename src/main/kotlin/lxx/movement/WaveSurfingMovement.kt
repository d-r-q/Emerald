package lxx.movement

import lxx.analysis.Collector
import lxx.model.BattleState
import lxx.waves.WavesWatcher
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
import lxx.analysis.RealWaveDataCollector
import lxx.analysis.WaveGfReconstructor
import lxx.analysis.RealWaveDataCollector.CollectionMode
import lxx.analysis.Profile
import lxx.model.PointLike
import lxx.movement.mech.OrbitalMovementMech
import lxx.movement.mech.OrbitDestination
import lxx.movement.mech.OrbitDirection
import lxx.movement.mech.GoToMovementMech
import java.util.HashMap

public class WaveSurfingMovement(val battleRules: BattleRules) : Collector, Movement {

    private val wavesWatcher = WavesWatcher(battleRules.enemyName, battleRules.myName)
    private val passedWaves = wavesWatcher.brokenWavesStream()

    private val orbMov = OrbitalMovementMech(battleRules.battleField, 800.0)

    private val goToMov = GoToMovementMech()

    private val destinations: HashMap<LxxWave, PointLike> = hashMapOf()

    class object {

        val dimensions = 5
        val tree = KdTree.SqrEuclid<Double>(dimensions, 35000)

        val locFormula: (LxxRobot, LxxRobot) -> DoubleArray = {(observer: LxxRobot, observable: LxxRobot) ->
            val res = DoubleArray(dimensions)
            res[0] = (observable.acceleration + 2) / 3 * 2
            res[1] = abs(lateralVelocity(observer, observable)) / Rules.MAX_VELOCITY * 4
            res[2] = advancingVelocity(observer, observable) / Rules.MAX_VELOCITY * 3
            res[3] = observer.distance(observable) / 800
            res[4] = observable.distanceToForwardWall() / 800 * 2

            res
        }

    }

    private val dataCollector = RealWaveDataCollector(locFormula, WaveGfReconstructor(battleRules.enemyName, battleRules.myName), tree, battleRules.enemyName, battleRules.myName, CollectionMode.HITS)

    override fun getMovementDecision(battleState: BattleState): MovementDecision {
        val wave = wavesWatcher.wavesInAir.first
        val dest =
                if (wave == null) battleRules.battleField.center
                else destinations.getOrPut(wave, { destination(battleState, wave) })

        return goToMov.getMovementDecision(battleState.me, dest)
    }

    override fun collectData(battleState: BattleState) {
        dataCollector.collectData(battleState)
        wavesWatcher.collectData(battleState)
        passedWaves.forEach { destinations.remove(it) }

        if (battleState.enemy.firePower != null && battleState.enemy.firePower > 0.0) {
            wavesWatcher.watch(LxxWave(battleState.prevState!!, battleRules.enemyName, battleRules.myName, Rules.getBulletSpeed(battleState.enemy.firePower)))
        }
    }

    private fun destination(currentState: BattleState, wave: LxxWave): PointLike {
        val bulletSpeed = Rules.getBulletSpeed(wave.speed)
        val data = dataCollector.getData(wave.battleState, bulletSpeed)
        val mea = lxx.model.getMaxEscapeAngle(wave.attacker, wave.victim, bulletSpeed)
        val profile = Profile(DoubleArray(191), mea.minAngle, mea.maxAngle)

        data.forEach {
            profile.addScore(it.first, it.second, 15)
        }

        fun pointDanger(pnt: PointLike) = profile.bearingOffsetDanger(wave.toBearingOffset(pnt))
        val dest = calculateFuturePositions(currentState, wave).minBy { pointDanger(it) }!!
        Canvas.PREDICTED_POSITIONS.setColor(Color.RED)
        Canvas.PREDICTED_POSITIONS.drawCircle(dest, 3.0)

        profile.drawProfile(Canvas.MY_MOVEMENT_PROFILE)

        return dest
    }

    private fun calculateFuturePositions(battleState: BattleState, wave: LxxWave): Stream<PointLike> {
        Canvas.PREDICTED_POSITIONS.reset()
        val cwPos = stream(battleState.me, pointsGenerator(OrbitDestination(battleState.enemy.prevState!!, OrbitDirection.CLOCKWISE))).
                takeWhile {
                    Canvas.PREDICTED_POSITIONS.setColor(Color.BLUE)
                    Canvas.PREDICTED_POSITIONS.fillCircle(it, 2.0)
                    !wave.isReached(it)
                }
        val ccwPos = stream(battleState.me, pointsGenerator(OrbitDestination(battleState.enemy.prevState, OrbitDirection.COUNTER_CLOCKWISE))).
                takeWhile {
                    Canvas.PREDICTED_POSITIONS.setColor(Color.GREEN)
                    Canvas.PREDICTED_POSITIONS.fillCircle(it, 2.0)
                    !wave.isReached(it)
                }

        return cwPos + ccwPos
    }

    fun pointsGenerator(orbitDestination: OrbitDestination) = {(robot: LxxRobot) ->
        val movementDecision = orbMov.getMovementDecision(robot, orbitDestination)
        robot.apply(movementDecision)
    }

}