package lxx.analysis

import lxx.model.BattleState
import ags.utils.KdTree
import lxx.model.LxxRobot

abstract class DataCollector<INPUT, OUTPUT, DATA>(
        protected val locFormula: (LxxRobot, LxxRobot) -> DoubleArray,
        protected val dataReconsturcor: DataReconstructor<INPUT, OUTPUT, DATA>,
        protected val tree: KdTree<OUTPUT>
) : Collector {

    fun getData(battleState: BattleState, bulletSpeed: Double): List<Pair<DATA, Double>>  {
        val dataPoints = tree.nearestNeighbor(getLocation(battleState), 100, true)!!
        return dataPoints.map {
            Pair(dataReconsturcor.reconstruct(battleState, it.value!!, bulletSpeed), 1 - (it.distance / dataPoints[0].distance))
        }
    }

    protected abstract fun getLocation(battleState: BattleState): DoubleArray

}