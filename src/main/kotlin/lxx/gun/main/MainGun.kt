package lxx.gun.main

import lxx.model.BattleState
import ags.utils.KdTree
import lxx.model.LxxRobot
import lxx.model.lateralVelocity
import robocode.Rules
import lxx.analysis.WaveGfReconstructor
import lxx.analysis.Collector
import lxx.analysis.Profile
import robocode.util.Utils
import lxx.model.advancingVelocity
import java.lang.Math.abs
import java.lang.Math.min
import lxx.paint.Canvas
import lxx.analysis.WaveDataCollector
import lxx.waves.WavesWatcher
import lxx.stat.Stat

class MainGun(private val myName: String,
              private val enemyName: String,
              private val wavesWatcher: WavesWatcher,
              private val stat: Stat) : Collector {

    private val minFirePower = 0.1

    private var profile: Profile? = null
    private var firePower: Double? = null

    class object {

        val tree = KdTree.SqrEuclid<Double>(5, 1200)

        val locFormula = {(observer: LxxRobot, observable: LxxRobot) ->
            doubleArray(observable.acceleration + 2 / 3 * 2,
                    abs(lateralVelocity(observer, observable)) / Rules.MAX_VELOCITY * 4,
                    advancingVelocity(observer, observable) / Rules.MAX_VELOCITY * 3,
                    observer.distance(observable) / 800,
                    observable.distanceToForwardWall() / 800 * 2)
        }

    }

    private val dataCollector = WaveDataCollector(locFormula, WaveGfReconstructor(myName, enemyName), tree, myName, enemyName, wavesWatcher.brokenWavesStream())

    public fun getTurnDecision(battleState: BattleState): GunDecision {

        firePower = selectFirePower(battleState)

        val fireAngle: Double
        if (battleState.me.turnsToFire() > 2.0 || battleState.enemy.energy == 0.0 || firePower == null) {
            fireAngle = battleState.me.angleTo(battleState.enemy)
            profile = null
        } else {
            if (profile == null) {
                profile = getProfile(battleState, Rules.getBulletSpeed(firePower!!))
                profile!!.drawProfile(Canvas.MY_TARGETING_PROFILE)
            }
            val bestBearingOffset = profile!!.getBestBearingOffset()
            fireAngle = battleState.me.angleTo(battleState.enemy) + bestBearingOffset
        }

        return GunDecision(Utils.normalRelativeAngle(fireAngle - battleState.me.gunHeading), firePower)
    }

    override public fun collectData(battleState: BattleState) {
        wavesWatcher.collectData(battleState)
        dataCollector.collectData(battleState)
    }

    private fun selectFirePower(battleState: BattleState): Double? {
        if (battleState.me.energy <= minFirePower) {
            return null
        }

        val myHitRate = stat.myHitRate(battleState)
        if (myHitRate == 0.0) {
            return minFirePower
        }

        val twoHitsPower = battleState.me.energy * myHitRate * 2

        // fire power = 3 when hit rate = 0.35 (hit rate >= 0.33 leads to energy increase)
        val hitRateFirePower = myHitRate * 8.571

        val killFirePower = battleState.enemy.energy / 4

        val firePower = min(twoHitsPower, min(hitRateFirePower, killFirePower))

        return if (firePower >= minFirePower) firePower else minFirePower
    }

    private fun getProfile(battleState: BattleState, bulletSpeed: Double): Profile {
        val data = dataCollector.getData(battleState, bulletSpeed)

        val mea = battleState.preciseMaxEscapeAngle(battleState.enemy, bulletSpeed)
        val profile = Profile(data, mea.minAngle, mea.maxAngle)

        return profile
    }

}