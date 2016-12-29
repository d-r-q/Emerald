package lxx.stat

import lxx.model.BattleState
import ags.utils.KdTree
import lxx.model.LxxRobot
import lxx.analysis.Collector
import lxx.waves.WaveWithOffset
import lxx.waves.HitWave
import lxx.waves.BrokenWave
import lxx.util.Debugger

class Stat(val myWavesStream: Sequence<WaveWithOffset>, val debugger: Debugger) : Collector {

    private val locFormula = {observer: LxxRobot, observable: LxxRobot ->
        doubleArrayOf(observer.distance(observable) / 800,
                observable.distanceToForwardWall() / 800)
    }

    companion object {
        private val tree = KdTree.SqrEuclid<Double>(2, 1200)
    }

    fun myHitRate(bs: BattleState): Double {
        val data = tree.nearestNeighbor(locFormula(bs.me, bs.enemy), Math.max(Math.sqrt(tree.size().toDouble()).toInt() * 2, 5), false)
        return when (data.size) {
            0 -> 0.0
            else -> data.fold(0.0) { sum, entry -> sum + entry.value } / data.size
        }
    }

    override fun collectData(battleState: BattleState) {
        myWavesStream.forEach {
            val hitRate = when (it) {
                is HitWave -> if (it.hitRobot) 1.0 else 0.0
                is BrokenWave -> 0.0
                else -> throw IllegalArgumentException("Unexpected wave $it")
            }
            tree.addPoint(locFormula(battleState.me, battleState.enemy), hitRate)
        }

        debugger.debugProperty("My Situational Hit Rate", myHitRate(battleState))
    }

}
