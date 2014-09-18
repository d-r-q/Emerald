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
import lxx.paint.Canvas
import lxx.analysis.RealWaveDataCollector
import lxx.analysis.RealWaveDataCollector.CollectionMode

class MainGun(val myName: String, val enemyName: String) : Collector {

    private var profile: Profile? = null
    private var firePower: Double? = null

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

    private val dataCollector = RealWaveDataCollector(locFormula, WaveGfReconstructor(myName, enemyName), tree, myName, enemyName, CollectionMode.VISITS)

    public fun getTurnDecision(battleState: BattleState): GunDecision {

        firePower = selectFirePower(battleState)

        val fireAngle: Double
        if (battleState.me.turnsToFire() > 2.0 || battleState.enemy.energy == 0.0) {
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
        dataCollector.collectData(battleState)
    }

    private fun selectFirePower([suppress("UNUSED_PARAMETER")] battleState: BattleState) = 1.95

    private fun getProfile(battleState: BattleState, bulletSpeed: Double): Profile {
        val mea = lxx.model.getMaxEscapeAngle(battleState.me, battleState.enemy, bulletSpeed)
        val profile = Profile(DoubleArray(191), mea.minAngle, mea.maxAngle)

        dataCollector.getData(battleState, bulletSpeed).forEach {
            profile.addScore(it.first, it.second)
        }

        return profile
    }

}